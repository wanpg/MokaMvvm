package com.moka

import android.content.Context
import android.os.Handler
import android.view.View
import android.widget.Toast

import com.moka.annotations.ObserveBy
import com.moka.mvvm.MVVM
import com.moka.mvvm.Observable
import com.moka.mvvm.ViewModel

/**
 * Created by wangjinpeng on 2017/11/9.
 */

class TestViewModel(context: Context) : ViewModel(context), TestViewProtocol {

    private val handler = Handler()

    @ObserveBy(TestViewProtocol.getHelloWordText)
    val info = Observable("hello world !!!!!!")

    @ObserveBy(TestViewProtocol.getButtonEnable, TestViewProtocol.getButtonText)
    val buttonInfo = Observable<ButtonInfo>()

    override fun onCreate() {
        super.onCreate()
        handler.postDelayed({
            info.set("和理论绿绿绿绿绿绿绿绿绿绿")
            buttonInfo.set(ButtonInfo("延时设置的button文案", false))
        }, 2000)
    }

    override fun onButton2Click(view: View?) {
        Toast.makeText(context, "点击了button2", Toast.LENGTH_SHORT).show()
    }

    fun getHelloWortText(view: View): Observable<String>? {
        return null
    }

    override fun getHelloWordText(): Observable<String> {
        return info
    }

    override fun getButtonText(): Observable<String> {
        val buttonInfo = this.buttonInfo.get()
        return MVVM
                .wrap<String>(this, buttonInfo?.text)
                .describe(ButtonInfo::class.java, "text")
                .create()
    }

    override fun getButtonEnable(): Observable<Boolean> {
        val buttonInfo = this.buttonInfo.get()
        return MVVM.wrap<Boolean>(this, buttonInfo?.isEnable)
                .describe(ButtonInfo::class.java, "enable")
                .create()
    }

    override fun requestData(boolean: Boolean) {

    }
}
