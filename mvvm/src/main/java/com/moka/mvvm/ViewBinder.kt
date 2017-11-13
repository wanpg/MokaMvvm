package com.moka.mvvm

import android.view.View
import org.jetbrains.annotations.Nullable
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

/**
 * Created by wangjinpeng on 2017/11/10.
 */

open abstract class ViewBinder {

    open interface CallBack<T> {
        fun bind(t: T?)
    }

    abstract fun initView(container: View)

    abstract fun dataBind()

    abstract fun getViewController(): ViewController

    abstract fun setViewModel(viewModel: ViewModel)

    open fun executeCommand(command: String, vararg args: Any) {
        val kFunction = this::class.memberFunctions.firstOrNull { it.name == command }
        val accessible = kFunction?.isAccessible
        kFunction?.isAccessible = true
        var argList = ArrayList<Any>()
        argList.add(this)
        argList.addAll(args)
        kFunction?.call(*argList.toArray())
        kFunction?.isAccessible = accessible ?: false
    }

    companion object {
        fun create(viewController: ViewController): ViewBinder {
            val tClass = viewController.javaClass
            // 获取全名
            val name = tClass.simpleName
            val suffix = "ViewBinderImpl"
            val replaceSuffix = "ViewController"
            val implName =
                    if (name.endsWith(replaceSuffix)) {
                        name.replace(replaceSuffix, suffix)
                    } else {
                        name + suffix
                    }
            val daoPackage = tClass.`package`.name

            try {
                val aClass = Class.forName(if (daoPackage.isEmpty()) implName else daoPackage + "." + implName)
                return aClass.getConstructor(tClass).newInstance(viewController) as ViewBinder
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
