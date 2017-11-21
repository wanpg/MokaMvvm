package com.moka.compile;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Created by wangjinpeng on 2017/3/21.
 */
public class ReflectUtils {

    /**
     * 获取所有的变量，包括父类的
     *
     * @param clazz    获取变量的类
     * @param endClazz 获取变量的终点
     * @return
     */
    public static List<Field> getAllFields(Class clazz, Class endClazz) {
        if (clazz == null || clazz.getName().equals(endClazz.getName())) {
            return new ArrayList<>();
        } else {
            List<Field> list = new ArrayList<>();
            Collections.addAll(list, clazz.getDeclaredFields());
            list.addAll(getAllFields(clazz.getSuperclass(), endClazz));
            return list;
        }
    }

    public static List<Element> getAllFieldWithAnnotation(ProcessingEnvironment processingEnv, TypeElement typeElement, Class<? extends Annotation> annotationType) {
        List<? extends Element> allMembers = processingEnv.getElementUtils().getAllMembers(typeElement);
        List<Element> elements = new ArrayList<>();
        for (Element element : allMembers) {
            if (element.getAnnotation(annotationType) != null) {
                elements.add(element);
            }
        }
        return elements;
    }

//    public static <T> CoreDao<T> getGeneratedEntityDaoImpl(Class<T> tClass) {
//        // 获取全名
//        String name = tClass.getSimpleName();
//        String suffix = "CoreDaoImpl";
//        String daoImplName = name + suffix;
//        String daoPackage = tClass.getPackage().getName();
//        //noinspection TryWithIdenticalCatches
//        try {
//            Class<?> aClass = Class.forName(daoPackage.isEmpty() ? daoImplName : daoPackage + "." + daoImplName);
//            return (CoreDao<T>) aClass.newInstance();
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException("cannot find implementation for "
//                    + tClass.getCanonicalName() + ". " + daoImplName + " does not exist");
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException("Cannot access the constructor"
//                    + tClass.getCanonicalName());
//        } catch (InstantiationException e) {
//            throw new RuntimeException("Failed to create an instance of "
//                    + tClass.getCanonicalName());
//        }
//    }

    public static boolean isElementClassBase(Element element) {
        String elementName = element.getSimpleName().toString();
        if (elementName.equals("getClass")
                || elementName.equals("hashCode")
                || elementName.equals("equals")
                || elementName.equals("toString")
                || elementName.equals("notify")
                || elementName.equals("notifyAll")
                || elementName.equals("wait")
                || elementName.equals("Companion")
                ) {
            return true;
        }
        return false;
    }


    static Pattern patternReturnT = Pattern.compile("<.*>");

    public static TypeName getReturnType(String returnTypeString) {
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
                return getTypeByName(mainString);
            }
        } else {
            return getTypeByName(returnTypeString);
        }
    }

    public static String[] getNextReturn(String returnTypeString) {
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

    public static List<TypeName> getParams(String paramString) {
        String[] nextReturn = getNextReturn(paramString);
        List<TypeName> typeNames = new ArrayList<>();
        for (String string : nextReturn) {
            typeNames.add(getReturnType(string));
        }
        return typeNames;
    }

    private static TypeName getTypeByName(String typeName) {
        if ("boolean".equals(typeName)) {
            return TypeName.BOOLEAN;
        } else if ("byte".equals(typeName)) {
            return TypeName.BYTE;
        } else if ("short".equals(typeName)) {
            return TypeName.SHORT;
        } else if ("int".equals(typeName)) {
            return TypeName.INT;
        } else if ("long".equals(typeName)) {
            return TypeName.LONG;
        } else if ("char".equals(typeName)) {
            return TypeName.CHAR;
        } else if ("float".equals(typeName)) {
            return TypeName.FLOAT;
        } else if ("double".equals(typeName)) {
            return TypeName.DOUBLE;
        }
        return ClassName.bestGuess(typeName);
    }
}
