package com.moka.mvvm

import android.content.Context
import android.support.annotation.IdRes
import android.view.View
import android.view.View.NO_ID

/**
 * Created by wangjinpeng on 2017/11/9.
 */
abstract class ViewController : VVMBase {

    private val container: View

    protected val viewBinder: ViewBinder

    constructor(context: Context, container: View) : super(context) {
        this.container = container
        @Suppress("LeakingThis")
        viewBinder = ViewBinder.create(this)
    }

    fun setViewModel(viewModel: ViewModel) {
        viewBinder.setViewModel(viewModel)
    }

    private val viewMap = HashMap<Int, View>()

    override fun onCreate() {
        super.onCreate()
        viewBinder.initView(container)
        bindView(container)
        viewBinder.dataBind()
        viewBinder.dataBindObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewMap.clear()
    }

    open fun bindView(view: View) {
        // do nothing
    }

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