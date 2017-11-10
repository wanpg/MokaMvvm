package com.moka.mvvm

import android.view.View
import java.lang.reflect.InvocationTargetException

/**
 * Created by wangjinpeng on 2017/11/10.
 */

abstract class Binder {

    abstract fun initView(container: View)

    abstract fun dataBind()

    abstract fun getViewBind(): ViewBind

    companion object {
        fun create(viewBind: ViewBind): Binder {
            val tClass = viewBind.javaClass
            // 获取全名
            val name = tClass.simpleName
            val suffix = "BinderImpl"
            val implName = name + suffix
            val daoPackage = tClass.`package`.name

            try {
                val aClass = Class.forName(if (daoPackage.isEmpty()) implName else daoPackage + "." + implName)
                return aClass.getConstructor(tClass).newInstance(viewBind) as Binder
            } catch (e: ClassNotFoundException) {
                throw RuntimeException("cannot find implementation for "
                        + tClass.canonicalName + ". " + implName + " does not exist")
            } catch (e: IllegalAccessException) {
                throw RuntimeException("Cannot access the constructor" + implName)
            } catch (e: InstantiationException) {
                throw RuntimeException("Failed to create an instance of " + implName)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("Failed to find a constructor of " + implName)
            } catch (e: InvocationTargetException) {
                throw RuntimeException("Failed to find a constructor of 11 " + implName)
            }

        }
    }
}
