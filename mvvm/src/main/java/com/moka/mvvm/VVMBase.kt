package com.moka.mvvm

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Handler

/**
 * Created by wangjinpeng on 2017/11/9.
 */
open class VVMBase : LifecycleObserver {

    val handler = Handler()

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    private fun onAny(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate()
            Lifecycle.Event.ON_START -> onStart()
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_PAUSE -> onPause()
            Lifecycle.Event.ON_STOP -> onStop()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            Lifecycle.Event.ON_ANY -> TODO()
        }
    }

    open fun onCreate() {

    }

    open fun onStart() {

    }

    open fun onResume() {

    }

    open fun onPause() {

    }

    open fun onStop() {

    }

    open fun onDestroy() {

    }

    fun post(runnable: () -> Unit) {
        postDelayed(runnable, 0)
    }

    fun postDelayed(runnable: () -> Unit, time: Long) {
        handler.postDelayed({ runnable() }, time)
    }
}