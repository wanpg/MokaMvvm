package com.moka.compile;

import com.moka.annotations.Binder;
import com.moka.annotations.Binders;
import com.moka.annotations.Mvvm;
import com.moka.annotations.ObserveBy;
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
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static com.moka.compile.MvvmProcessor.classObservable;

/**
 * Created by wangjinpeng on 2017/11/14.
 */

public class DataBindMethod extends BaseMethod {

    public DataBindMethod(ProcessingEnvironment processingEnv, TypeElement typeElement) {
        super(processingEnv, typeElement);
    }

    @Override
    public MethodSpec build() {
        Mvvm mvvmAnnotation = typeElement.getAnnotation(Mvvm.class);
        TypeElement viewModelElement = null;
        try {
            ClassName.get(mvvmAnnotation.viewModel());
        } catch (MirroredTypeException e) {
            viewModelElement = (TypeElement) processingEnv.getTypeUtils().asElement(e.getTypeMirror());
        }

        TypeElement viewProtocolElement = null;
        try {
            ClassName.get(mvvmAnnotation.protocol());
        } catch (MirroredTypeException e) {
            viewProtocolElement = (TypeElement) processingEnv.getTypeUtils().asElement(e.getTypeMirror());
        }

        List<? extends Element> modelElements = getProtocolElementInModel(viewModelElement, viewProtocolElement);

        MethodSpec.Builder dataBindBuilder = MethodSpec.methodBuilder("dataBind")
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
                for (Element element : modelElements) {
                    String simpleName = element.getSimpleName().toString();
                    System.out.println("输出的element是" + simpleName);
                    if (simpleName.toUpperCase().contains(methodString.toUpperCase())) {
                        dataBindBuilder.addCode("        viewModel.$N();\n", simpleName);
                    }
                }
            }
            dataBindBuilder.addCode("        return null;\n")
                    .addCode("    }\n")
                    .addCode("});\n");
        }

        List<Element> allFieldWithBinder = new ArrayList<>();
        allFieldWithBinder.addAll(ReflectUtils.getAllFieldWithAnnotation(processingEnv, typeElement, Binder.class));
        allFieldWithBinder.addAll(ReflectUtils.getAllFieldWithAnnotation(processingEnv, typeElement, Binders.class));

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "classObservable to string " + classObservable.toString());
        for (Element elementProtocol : modelElements) {
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
                            if (simpleName.toUpperCase().contains(binder.observe().toUpperCase())) {
                                binderMethod.add(binderElement.getSimpleName() + ".set" + binder.property().substring(0, 1).toUpperCase() + binder.property().substring(1) + "(%s);\n");
                            }
                        } else {
                            Binders binders = binderElement.getAnnotation(Binders.class);
                            if (binders != null) {
                                Binder[] values = binders.value();
                                for (Binder binderValue : values) {
                                    if (simpleName.toUpperCase().contains(binderValue.observe().toUpperCase())) {
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
                    } else {
                        dataBindBuilder.addCode("$T $N = viewModel.$N();\n", typeName, simpleName, simpleName);
                        for (String methodInvoke : binderMethod) {
                            dataBindBuilder.addCode("viewController." + String.format(methodInvoke, simpleName));
                        }
                    }
                }
            }
        }
        return dataBindBuilder.build();
    }

    private List<Element> getProtocolElementInModel(TypeElement modelElement, TypeElement protocolElement) {
        List<? extends Element> modelElements = processingEnv.getElementUtils().getAllMembers(modelElement);
        List<? extends Element> protocolElements = processingEnv.getElementUtils().getAllMembers(protocolElement);
        List<Element> result = new ArrayList<>();
        for (Element eInModel : modelElements) {
            if (!isElementClassBase(eInModel) && eInModel.getKind() == ElementKind.METHOD) {
                Name eInModelSimpleName = eInModel.getSimpleName();
                for (Element eInProtocol : protocolElements) {
                    if (eInProtocol.getKind() == ElementKind.METHOD
                            && eInModelSimpleName.equals(eInProtocol.getSimpleName())) {
                        result.add(eInModel);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean isElementClassBase(Element element) {
        String elementName = element.getSimpleName().toString();
        if (elementName.equals("getClass")
                || elementName.equals("hashCode")
                || elementName.equals("equals")
                || elementName.equals("toString")
                || elementName.equals("notify")
                || elementName.equals("notifyAll")
                || elementName.equals("wait")) {
            return true;
        }
        return false;
    }

    Pattern patternReturnT = Pattern.compile("<.*>");

    private TypeName getReturnType(String returnTypeString) {
        Matcher matcher = patternReturnT.matcher(returnTypeString);
        if (matcher.find()) {
            String group = matcher.group();

            String substring = group.substring(1, group.length() - 1);
            String[] tStrings = getNextReturn(substring);
            TypeName[] tTypeNames = new TypeName[tStrings.length];
            for (int i = 0; i < tStrings.length; i++) {
                tTypeNames[i] = getReturnType(tStrings[i]);
            }

            String mainString = returnTypeString.replace(group, "");
            if (tTypeNames.length > 0) {
                return ParameterizedTypeName.get(ClassName.bestGuess(mainString), tTypeNames);
            } else {
                return ClassName.bestGuess(mainString);
            }
        } else {
            return ClassName.bestGuess(returnTypeString);
        }
    }

    private static String[] getNextReturn(String returnTypeString) {
        List<String> strings = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int level = 0;
        for (int i = 0; i < returnTypeString.length(); i++) {
            String indexStr = returnTypeString.substring(i, i + 1);
            if (",".equals(indexStr)) {
                if (level == 0) {
                    strings.add(sb.toString());
                    sb = new StringBuilder();
                    continue;
                }
            }

            sb.append(indexStr);
            if ("<".equals(indexStr)) {
                level++;
            } else if (">".equals(indexStr)) {
                level--;
            }
        }
        if (sb.length() > 0) {
            strings.add(sb.toString());
        }
        String[] result = new String[strings.size()];
        strings.toArray(result);
        return result;
    }
}
