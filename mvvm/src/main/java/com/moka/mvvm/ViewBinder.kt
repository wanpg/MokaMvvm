package com.moka.mvvm

/**
 * Created by wangjinpeng on 2017/11/9.
 */
interface ViewBinder<T> {
    fun bind(t: T)
}