package com.moka.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangjinpeng on 2017/11/10.
 */

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Binder {

    /**
     * view的属性， 比如 TextView的text，View的enable 等
     *
     * @return
     */
    String property() default "";

    String observe();
}
