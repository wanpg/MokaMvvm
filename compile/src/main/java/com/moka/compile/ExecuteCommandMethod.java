package com.moka.compile;

import com.moka.annotations.Mvvm;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

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

/**
 * Created by wangjinpeng on 2017/11/16.
 */

public class ExecuteCommandMethod extends BaseMethod {

    TypeElement viewModelElement = null;
    TypeElement viewProtocolElement = null;
    List<Element> allStaticField = new ArrayList<>();
    List<Element> allMethod = new ArrayList<>();

    public ExecuteCommandMethod(ProcessingEnvironment processingEnv, TypeElement typeElement) {
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
        TypeVariableName typeVariableName = TypeVariableName.get("Object...", Object.class);
        MethodSpec.Builder builder = MethodSpec.methodBuilder("executeCommand")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "command")
                .addParameter(typeVariableName, "args");


        // 此处执行 返回值是void的方法

        boolean hasCommand = false;
        for (Element element : allMethod) {
            String simpleName = element.getSimpleName().toString();
            TypeMirror protocolTypeMirror = element.asType();
            String protocolMethodString = protocolTypeMirror.toString();

            Pattern patternParams = Pattern.compile("\\(.*\\)");

            Matcher matcherParams = patternParams.matcher(protocolMethodString);
            if (matcherParams.find()) {
                String paramString = matcherParams.group();

                String returnString = protocolMethodString.replace(paramString, "");

                if ("void".equalsIgnoreCase(returnString)) {
                    for (Element staticField : allStaticField) {
                        String staticFieldString = staticField.getSimpleName().toString();
                        if (simpleName.equals(staticFieldString)) {
                            hasCommand = true;
                            builder.addCode("if ($T.$N.equals(command)) {\n", viewProtocolElement, staticFieldString);
                            builder.addCode("    viewModel.$N(", simpleName);

                            List<TypeName> params = ReflectUtils.getParams(paramString.replace("(", "").replace(")", ""));
                            for (int i = 0; i < params.size(); i++) {
                                TypeName typeName = params.get(i);
                                String indexStr = String.valueOf(i);
//                                builder.addCode("args.length > $N ? ($T) args[$N] : null", indexStr, typeName, indexStr, )
                                if (i > 0) {
                                    builder.addCode(", ");
                                }
                                builder.addCode("($T) args[$N]", typeName, indexStr);
                            }
                            builder.addCode(");\n");
                            break;
                        }
                    }
                }
            }
        }

        if (hasCommand) {
            builder.addCode("} else {\n");
            builder.addCode("    ");
        }
        builder.addCode("super.executeCommand(command, args);\n");
        if (hasCommand) {
            builder.addCode("}\n");
        }

        return builder.build();
    }
}
