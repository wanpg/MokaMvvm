package com.moka.compile;

import com.moka.annotations.Binder;
import com.moka.annotations.Binders;
import com.moka.annotations.Mvvm;
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
import static com.moka.compile.ReflectUtils.getReturnType;

/**
 * dataBind 方法的生成
 * <p>
 * 1、 绑定ViewModel中的field 与 protocol方法
 * 2、 绑定ViewController中的field 与protocol方法
 * 3、 绑定ViewController中的field 执行command 调用protocol方法
 */

public class DataBindMethod extends BaseMethod {
    TypeElement viewProtocolElement = null;
    TypeElement viewModelElement = null;
    List<Element> allStaticField = new ArrayList<>();
    List<Element> allMethod = new ArrayList<>();

    public DataBindMethod(ProcessingEnvironment processingEnv, TypeElement typeElement) {
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
        MethodSpec.Builder dataBindBuilder = MethodSpec.methodBuilder("dataBind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

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
                        dataBindBuilder.addCode("$T $N = viewModel.$N().get();\n", typeNameT, simpleName, simpleName);
                    } else {
                        dataBindBuilder.addCode("$T $N = viewModel.$N();\n", typeName, simpleName, simpleName);
                    }
                    for (String methodInvoke : binderMethod) {
                        dataBindBuilder.addCode("viewController." + String.format(methodInvoke, simpleName));
                    }
                }
            }
        }

        return dataBindBuilder.build();
    }
}
