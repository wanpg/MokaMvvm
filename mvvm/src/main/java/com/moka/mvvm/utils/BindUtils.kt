package com.moka.mvvm.utils

import android.widget.TextView
import com.moka.mvvm.*
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

    fun <T> bind(model: ViewModel, modelProperty: String, callBack: ViewBinder.CallBack<T>) {
        val split = modelProperty.split(".")
        val kProperty1 = model::class.memberProperties.firstOrNull { it.name == split[0] }
        val access = kProperty1?.isAccessible
        kProperty1?.isAccessible = true
        var info = kProperty1?.getter?.call(model)
        kProperty1?.isAccessible = access ?: false
        if (info is Observable<*>) {
            callBack.bind(info.get() as T)
            info.addObserver { t ->
                model.post {
                    callBack.bind(t as T)
                }
            }
        } else if (info is CharSequence) {
            callBack.bind(info as T)
        }
    }

    open fun <T> bind(observable: Observable<T>, observer: (t: T) -> Unit) {
        observable.addObserver { t ->
            observer(t)
        }
    }
}
