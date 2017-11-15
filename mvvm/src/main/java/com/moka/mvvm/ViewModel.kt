package com.moka.mvvm

import android.content.Context

/**
 * Created by wangjinpeng on 2017/11/9.
 */
open class ViewModel(context: Context) : VVMBase(context) {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        MVVM.clear(this)
    }
}