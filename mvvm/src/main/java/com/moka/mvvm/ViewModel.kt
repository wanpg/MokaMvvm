package com.moka.mvvm

import android.widget.TextView
import com.moka.mvvm.utils.BindUtils

/**
 * Created by wangjinpeng on 2017/11/9.
 */
open class ViewModel : VVMBase() {

    override fun onCreate() {
        super.onCreate()
    }

    fun <T> bind(field: String, binder: ViewBinder<T>) {
        BindUtils.bind(this, field, binder)
    }

    fun bindText(textView: TextView, field: String) {
        BindUtils.bindText(textView, this, field)
    }
}