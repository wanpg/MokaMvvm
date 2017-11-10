package com.moka.mvvm.utils

import android.widget.TextView
import com.moka.mvvm.Observable
import com.moka.mvvm.Observer
import com.moka.mvvm.ViewBinder
import com.moka.mvvm.ViewModel
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Created by wangjinpeng on 2017/11/10.
 */

object BindUtils {

    fun bindText(textView: TextView, model: ViewModel, field: String) {
        val split = field.split(".")
        val kProperty1 = model::class.memberProperties.firstOrNull { it.name == split[0] }
        val access = kProperty1?.isAccessible
        kProperty1?.isAccessible = true
        var info = kProperty1?.getter?.call(model)
        kProperty1?.isAccessible = access ?: false
        if (info is Observable<*>) {
            textView.text = info.get() as CharSequence
            info.addObserver(object : Observer<String> {
                override fun update(t: String) {
                    textView.text = t
                }
            })
        } else if (info is CharSequence) {
            textView.text = info
        }
    }

    fun <T> bind(model: ViewModel, modelProperty: String, binder: ViewBinder<T>) {
        val split = modelProperty.split(".")
        val kProperty1 = model::class.memberProperties.firstOrNull { it.name == split[0] }
        val access = kProperty1?.isAccessible
        kProperty1?.isAccessible = true
        var info = kProperty1?.getter?.call(model)
        kProperty1?.isAccessible = access ?: false
        if (info is Observable<*>) {
            binder.bind(info.get() as T)
            info.addObserver { t ->
                model.post {
                    binder.bind(t as T)
                }
            }
        } else if (info is CharSequence) {
            binder.bind(info as T)
        }
    }
}
