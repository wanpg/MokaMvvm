package com.moka.mvvm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangjinpeng on 2017/11/14.
 */

public class MVVM {

    private static final Map<Object, Map<String, Observable>> observablePool = new HashMap<>();

    /**
     * 包装
     *
     * @param object
     * @param t
     * @param <T>
     * @return
     */
    public static <T> MVVMWrap<T> wrap(Object object, T t) {
        return new MVVMWrap<>(object, t);
    }

    public static void clear(Object object) {
        observablePool.remove(object);
    }

    public static class MVVMWrap<T> {

        private Object object;
        private T value;

        private String key;

        public MVVMWrap(Object object, T value) {
            this.object = object;
            this.value = value;
        }

        /**
         * 描述
         *
         * @param aClass
         * @param fieldName
         */
        public MVVMWrap<T> describe(Class<?> aClass, String fieldName) {
            key = aClass.getName() + "." + fieldName;
            return this;
        }

        public Observable<T> create() {
            Map<String, Observable> stringObservableMap = observablePool.get(object);
            if (stringObservableMap == null) {
                stringObservableMap = new HashMap<>();
                observablePool.put(object, stringObservableMap);
            }
            Observable<T> observable = stringObservableMap.get(key);
            if (observable == null) {
                observable = new Observable<>();
                stringObservableMap.put(key, observable);
            }
            observable.set(value);
            return observable;
        }
    }
}
