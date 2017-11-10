package com.moka.mvvm

import java.util.Observable

/**
 * Created by wangjinpeng on 2017/11/9.
 */
open interface Observer<in T> : java.util.Observer {

    override fun update(o: Observable?, arg: Any?) {
        @Suppress("UNCHECKED_CAST")
        val value: T = arg as T
        update(value)
    }

    fun update(t: T)
}