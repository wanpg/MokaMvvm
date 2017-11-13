package com.moka.compile;

import com.moka.annotations.Mvvm;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class MvvmProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(Mvvm.class);
        for (Element element : elementsAnnotatedWith) {
            if (element instanceof TypeElement) {
            }
        }
        return false;
    }
}
