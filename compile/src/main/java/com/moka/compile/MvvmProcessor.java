package com.moka.compile;

import com.moka.annotations.Mvvm;
import com.moka.annotations.ObserveBy;
import com.moka.annotations.ViewId;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@SupportedAnnotationTypes("com.moka.annotations.Mvvm")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MvvmProcessor extends AbstractProcessor {

    public static final ClassName classViewBinder = ClassName.bestGuess("com.moka.mvvm.ViewBinder");
    public static final ClassName classViewModel = ClassName.bestGuess("com.moka.mvvm.ViewModel");
    public static final ClassName classView = ClassName.bestGuess("android.view.View");
    public static final ClassName classKFunction1 = ClassName.bestGuess("kotlin.jvm.functions.Function1");
    public static final ClassName classKUnit = ClassName.bestGuess("kotlin.Unit");
    public static final ClassName classObservable = ClassName.bestGuess("com.moka.mvvm.Observable");

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(Mvvm.class);
        for (Element element : elementsAnnotatedWith) {
            if (element instanceof TypeElement) {
                try {
                    createViewBinder((TypeElement) element);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void createViewBinder(TypeElement element) throws IOException {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();

        // viewController的名字
        String controllerName = element.getSimpleName().toString();
        // viewController在的packageName
        String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();

        String suffix = "ViewBinderImpl";
        String replaceSuffix = "ViewController";
        String implName;
        if (controllerName.endsWith(replaceSuffix)) {
            implName = controllerName.replace(replaceSuffix, suffix);
        } else {
            implName = controllerName + suffix;
        }

        Mvvm mvvmAnnotation = element.getAnnotation(Mvvm.class);

        TypeName protocolType;
        try {
            protocolType = ClassName.get(mvvmAnnotation.protocol());
        } catch (MirroredTypeException e) {
            protocolType = ClassName.get(e.getTypeMirror());
        }

        TypeName viewModelType;
        try {
            viewModelType = ClassName.get(mvvmAnnotation.viewModel());
        } catch (MirroredTypeException e) {
            viewModelType = ClassName.get(e.getTypeMirror());
        }

        TypeSpec.Builder viewBinderTypeBuilder = TypeSpec.classBuilder(implName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(classViewBinder);

        // 1. 创建Controller field
        viewBinderTypeBuilder.addField(ClassName.get(element), "viewController", Modifier.PRIVATE);
        // 2. 创建ViewModel field
        viewBinderTypeBuilder.addField(viewModelType, "viewModel", Modifier.PRIVATE);
        // 3. 创建 构造函数
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(element), "viewController")
                .addStatement("this.$N = $N", "viewController", "viewController")
                .build();
        viewBinderTypeBuilder.addMethod(constructor);

        // 4. 创建 override setViewModel 方法
        MethodSpec setViewModel = MethodSpec.methodBuilder("setViewModel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(classViewModel, "viewModel")
                .addStatement("this.$N = ($T) $N", "viewModel", viewModelType, "viewModel")
                .build();
        viewBinderTypeBuilder.addMethod(setViewModel);

        // 5. 创建initView方法
        MethodSpec.Builder initViewBuilder = MethodSpec.methodBuilder("initView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(classView, "container");
        List<Element> allFieldWithViewId = ReflectUtils.getAllFieldWithAnnotation(processingEnv, element, ViewId.class);
        for (Element viewElement : allFieldWithViewId) {
            String viewName = viewElement.getSimpleName().toString();
            ViewId viewAnnotation = viewElement.getAnnotation(ViewId.class);
            int value = viewAnnotation.value();// FIXME: 2017/11/14 此处后面需要替换为 R.id.xxx的格式
            initViewBuilder.addStatement("viewController.$N = container.findViewById($N)", viewName, String.valueOf(value));
        }
        // 检索controller中所有的 viewId 的注解
        viewBinderTypeBuilder.addMethod(initViewBuilder.build());

        // 6. 创建 dataBind 方法
        viewBinderTypeBuilder.addMethod(new DataBindMethod(processingEnv, element).build());

        // 7. 创建 executeCommand 方法
        MethodSpec.Builder executeCommandBuilder = MethodSpec.methodBuilder("executeCommand")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "command")
                .addParameter(Object[].class, "args");

        viewBinderTypeBuilder.addMethod(executeCommandBuilder.build());

        JavaFile javaFile = JavaFile.builder(packageName, viewBinderTypeBuilder.build()).build();
        javaFile.writeTo(processingEnv.getFiler());
        System.out.println(element.getSimpleName());
        System.out.println(elementUtils.getPackageOf(element).getQualifiedName());
    }
}
