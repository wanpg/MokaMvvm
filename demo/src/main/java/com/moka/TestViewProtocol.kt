package com.moka

import android.view.View
import com.moka.mvvm.ViewProtocol
import com.moka.mvvm.Observable

/**
 * Created by wangjinpeng on 2017/11/13.
 */

interface TestViewProtocol : ViewProtocol {

    companion object {
        const val helloWordText = "helloWordText"
        const val buttonText = "buttonText"
        const val buttonEnable = "buttonEnable"
        const val onButton2Click = "onButton2Click"
    }

    val helloWordText: Observable<String>

    val buttonText: Observable<String>

    val buttonEnable: Observable<Boolean>

    fun onButton2Click(view: View?)
}
