package com.moka.mvvm

/**
 * Created by wangjinpeng on 2017/11/9.
 */

open class Observable<T>(private var value: T? = null) : java.util.Observable() {

    open fun set(t: T?) {
        this.value = t
        notifyChanged()
    }

    open fun get(): T? = value

    fun addObserver(observer: (t: T) -> Unit) {
        addObserver { _, arg -> observer(arg as T) }
    }

    fun notifyChanged() {
        setChanged()
        notifyObservers(value)
    }
}
