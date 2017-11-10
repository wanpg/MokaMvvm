package com.moka.mvvm.annotation;

import android.support.annotation.IdRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangjinpeng on 2017/11/10.
 */

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bind {

    /**
     * 指定的viewId
     *
     * @return
     */
    @IdRes int viewId();

    String bindProp();
}
