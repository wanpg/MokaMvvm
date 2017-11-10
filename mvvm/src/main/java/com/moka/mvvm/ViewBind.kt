package com.moka.mvvm

import android.support.annotation.IdRes
import android.view.View
import android.view.View.NO_ID

/**
 * Created by wangjinpeng on 2017/11/9.
 */
abstract class ViewBind : VVMBase {

    private val container: View

    private val binder: Binder

    constructor(container: View) {
        this.container = container
        @Suppress("LeakingThis")
        binder = Binder.create(this)
    }

    private val viewMap = HashMap<Int, View>()

    lateinit var viewModel: ViewModel

    override fun onCreate() {
        super.onCreate()
        binder.initView(container)
        bindView(container)
        binder.dataBind()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewMap.clear()
    }

    abstract fun bindView(view: View)

    fun <T : View> findViewById(@IdRes id: Int): T? {
        return if (id == NO_ID) {
            null
        } else {
            var view = viewMap[id]
            if (view == null) {
                view = container.findViewById(id)
                if (view != null) {
                    viewMap.put(id, view)
                }
            }
            return view as T?
        }
    }
}