package com.moka.compile;

import com.moka.annotations.Binder;
import com.moka.annotations.Binders;
import com.moka.annotations.Command;
import com.moka.annotations.Commands;
import com.moka.annotations.Mvvm;
import com.moka.annotations.ObserveBy;
import com.moka.mvvm.ViewProperty;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static com.moka.compile.MvvmProcessor.classObservable;
import static com.moka.compile.MvvmProcessor.classView;
import static com.moka.compile.ReflectUtils.getReturnType;

/**
 * dataBind 方法的生成
 * <p>
 * 1、 绑定ViewModel中的field 与 protocol方法
 * 2、 绑定ViewController中的field 与protocol方法
 * 3、 绑定ViewController中的field 执行command 调用protocol方法
 */

public class DataBindObserverMethod extends BaseMethod {
    TypeElement viewProtocolElement = null;
    TypeElement viewModelElement = null;
    List<Element> allStaticField = new ArrayList<>();
    List<Element> allMethod = new ArrayList<>();

    public DataBindObserverMethod(ProcessingEnvironment processingEnv, TypeElement typeElement) {
        super(processingEnv, typeElement);
        Mvvm mvvmAnnotation = typeElement.getAnnotation(Mvvm.class);

        try {
            ClassName.get(mvvmAnnotation.viewModel());
        } catch (MirroredTypeException e) {
            viewModelElement = (TypeElement) processingEnv.getTypeUtils().asElement(e.getTypeMirror());
        }

        try {
            ClassName.get(mvvmAnnotation.protocol());
        } catch (MirroredTypeException e) {
            viewProtocolElement = (TypeElement) processingEnv.getTypeUtils().asElement(e.getTypeMirror());
        }

//        List<? extends Element> modelElements = getProtocolElementInModel(viewModelElement, viewProtocolElement);

        List<? extends Element> allProtocolElements = processingEnv.getElementUtils().getAllMembers(viewProtocolElement);

        for (Element element : allProtocolElements) {
            if (!ReflectUtils.isElementClassBase(element)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "element : " + element.toString());
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "element kind: " + element.getKind());
                if (element.getKind() == ElementKind.METHOD) {
                    allMethod.add(element);
                } else if (element.getKind() == ElementKind.FIELD) {
                    allStaticField.add(element);
                }
            }
        }
    }

    @Override
    public MethodSpec build() {
        MethodSpec.Builder dataBindBuilder = MethodSpec.methodBuilder("dataBindObserver")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        // 先处理ViewModel的绑定
        List<Element> allFieldWithObserveBy = ReflectUtils.getAllFieldWithAnnotation(processingEnv, viewModelElement, ObserveBy.class);
        for (Element elementObserveBy : allFieldWithObserveBy) {
            TypeName typeName = ClassName.get(elementObserveBy.asType());
            if (typeName instanceof ParameterizedTypeName) {
                List<TypeName> typeArguments = ((ParameterizedTypeName) typeName).typeArguments;
                typeName = typeArguments.get(0);
            }
            ObserveBy observeBy = elementObserveBy.getAnnotation(ObserveBy.class);
            String[] value = observeBy.value();

            String fieldName = elementObserveBy.getSimpleName().toString();
            String methodName = elementObserveBy.getModifiers().contains(Modifier.PUBLIC) ? fieldName : "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "()";
            dataBindBuilder
                    .addCode("viewModel.$N.addObserver(new $T<$T, $T>() {\n", methodName, MvvmProcessor.classKFunction1, typeName, MvvmProcessor.classKUnit)
                    .addCode("    @Override\n")
                    .addCode("    public $T invoke($T data) {\n", MvvmProcessor.classKUnit, typeName);
            for (String methodString : value) {
                for (Element element : allMethod) {
                    String simpleName = element.getSimpleName().toString();
                    System.out.println("输出的element是" + simpleName);
                    if (simpleName.equals(methodString)) {
                        dataBindBuilder.addCode("        viewModel.$N();\n", simpleName);
                    }
                }
            }
            dataBindBuilder.addCode("        return null;\n")
                    .addCode("    }\n")
                    .addCode("});\n");
        }

        // 此处处理ViewController Bind
        List<Element> allFieldWithBinder = new ArrayList<>();
        allFieldWithBinder.addAll(ReflectUtils.getAllFieldWithAnnotation(processingEnv, typeElement, Binder.class));
        allFieldWithBinder.addAll(ReflectUtils.getAllFieldWithAnnotation(processingEnv, typeElement, Binders.class));

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "classObservable to string " + classObservable.toString());
        for (Element elementProtocol : allMethod) {
            String simpleName = elementProtocol.getSimpleName().toString();
            TypeMirror protocolTypeMirror = elementProtocol.asType();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "protocolTypeMirror : " + protocolTypeMirror);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "protocolTypeMirror : " + protocolTypeMirror.getClass().toString());

            String protocolMethodString = protocolTypeMirror.toString();

            Pattern patternParams = Pattern.compile("\\(.*\\)");

            Matcher matcherParams = patternParams.matcher(protocolMethodString);
            if (matcherParams.find()) {
                String paramString = matcherParams.group();
                String[] strings = paramString.replace("(", "").replace(")", "").split(",");

                String returnString = protocolMethodString.replace(paramString, "");

                if (!"void".equalsIgnoreCase(returnString)) {
                    TypeName typeName = getReturnType(returnString);
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "typeName : " + typeName.toString());

                    List<String> binderMethod = new ArrayList<>();
                    for (Element binderElement : allFieldWithBinder) {
                        Binder binder = binderElement.getAnnotation(Binder.class);
                        if (binder != null) {
                            if (simpleName.equals(binder.observe())) {
                                binderMethod.add(binderElement.getSimpleName() + ".set" + binder.property().substring(0, 1).toUpperCase() + binder.property().substring(1) + "(%s);\n");
                            }
                        } else {
                            Binders binders = binderElement.getAnnotation(Binders.class);
                            if (binders != null) {
                                Binder[] values = binders.value();
                                for (Binder binderValue : values) {
                                    if (simpleName.equals(binderValue.observe())) {
                                        binderMethod.add(binderElement.getSimpleName() + ".set" + binderValue.property().substring(0, 1).toUpperCase() + binderValue.property().substring(1) + "(%s);\n");
                                    }
                                }
                            }
                        }
                    }

                    // 此处判断是否是Observable，是的话添加观察者
                    // 如果不是，可以直接赋值
                    if (typeName.toString().startsWith(classObservable.toString())) {
                        TypeName typeNameT = ClassName.get(Object.class);
                        if (typeName instanceof ParameterizedTypeName) {
                            List<TypeName> typeArguments = ((ParameterizedTypeName) typeName).typeArguments;
                            typeNameT = typeArguments.get(0);
                        }
                        dataBindBuilder
                                .addCode("viewModel.$N().addObserver(new $T<$T, $T>() {\n", simpleName, MvvmProcessor.classKFunction1, typeNameT, MvvmProcessor.classKUnit)
                                .addCode("    @Override\n")
                                .addCode("    public $T invoke($T data) {\n", MvvmProcessor.classKUnit, typeNameT);
//                                .addCode("        viewController.button1.setEnabled(aBoolean);\n")

                        for (String methodInvoke : binderMethod) {
                            dataBindBuilder.addCode("        viewController." + String.format(methodInvoke, "data"));
                        }

                        dataBindBuilder.addCode("        return null;\n")
                                .addCode("    }\n")
                                .addCode("});\n");
                    } /*else {
                        dataBindBuilder.addCode("$T $N = viewModel.$N();\n", typeName, simpleName, simpleName);
                        for (String methodInvoke : binderMethod) {
                            dataBindBuilder.addCode("viewController." + String.format(methodInvoke, simpleName));
                        }
                    }*/
                }
            }
        }

        // 此处处理ViewController 的 Command的调用
        bindCommand(dataBindBuilder);

        return dataBindBuilder.build();
    }

    private void bindCommand(MethodSpec.Builder builder) {
        List<Element> commandElements = ReflectUtils.getAllFieldWithAnnotation(processingEnv, typeElement, Command.class);
        for (Element element : commandElements) {
            Command command = element.getAnnotation(Command.class);
            createOneCommandCode(builder, command, element);
        }
        List<Element> commandsElements = ReflectUtils.getAllFieldWithAnnotation(processingEnv, typeElement, Commands.class);
        for (Element element : commandsElements) {
            Commands commands = element.getAnnotation(Commands.class);
            if (commands != null) {
                Command[] value = commands.value();
                for (Command command : value) {
                    createOneCommandCode(builder, command, element);
                }
            }
        }
    }

    private void createOneCommandCode(MethodSpec.Builder builder, Command command, Element element) {
        if (command == null) {
            return;
        }
        String property = command.property();
        if (ViewProperty.View.onClick.equals(property)) {
            String execute = command.execute();
            builder.addCode("viewController.$N.setOnClickListener(new $T.OnClickListener() {\n", element.getSimpleName(), classView)
                    .addCode("    @Override\n")
                    .addCode("    public void onClick($T v) {\n", classView)
                    .addCode("        executeCommand($S, v);\n", execute)
                    .addCode("    }\n")
                    .addCode("});\n");
        }
    }
}
