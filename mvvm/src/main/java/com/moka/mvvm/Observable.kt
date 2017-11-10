package com.moka.mvvm

/**
 * Created by wangjinpeng on 2017/11/9.
 */

open class Observable<T>(private var value: T? = null) : java.util.Observable() {

    open fun set(t: T?) {
        this.value = t
        setChanged()
        notifyObservers(t)
    }

    open fun get(): T? {
        return value
    }

    fun addObserver(observer: (t: T) -> Unit) {
        addObserver { o, arg -> observer(arg as T) }
    }
}
