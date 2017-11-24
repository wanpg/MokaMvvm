package com.moka

import android.view.View
import com.moka.mvvm.ViewProtocol
import com.moka.mvvm.Observable

/**
 * Created by wangjinpeng on 2017/11/13.
 */

interface TestViewProtocol : ViewProtocol {

    companion object {
        const val getHelloWordText = "getHelloWordText"
        const val getButtonText = "getButtonText"
        const val getButtonText1 = "getButtonText1"
        const val getButtonEnable = "getButtonEnable"
        const val onButton2Click = "onButton2Click"
        const val requestData = "onButton2Click"
    }

    fun getHelloWordText(): Observable<String>

    fun getButtonText(): Observable<String>
    fun getButtonText1(): String

    fun getButtonEnable(): Observable<Boolean>

    fun onButton2Click(view: View?)

    fun requestData(boolean: Boolean)
}
