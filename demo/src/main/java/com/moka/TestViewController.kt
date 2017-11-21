package com.moka

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.moka.annotations.*
import com.moka.mvvm.ViewController
import com.moka.mvvm.ViewProperty

/**
 * Created by wangjinpeng on 2017/11/9.
 */
@Mvvm(protocol = TestViewProtocol::class, viewModel = TestViewModel::class)
open class TestViewController(context: Context, container: View) : ViewController(context, container) {

    @ViewId(R.id.hello_world)
//    @Binder(property = ViewProperty.TextView.text, observe = TestViewProtocol.helloWordText)
    open lateinit var infoText: TextView

    @ViewId(R.id.button1)
    @Binders(Binder(property = ViewProperty.Button.text, observe = TestViewProtocol.getButtonText),
            Binder(property = ViewProperty.Button.enabled, observe = TestViewProtocol.getButtonEnable))
    open lateinit var button1: Button

    @ViewId(R.id.button2)
    @Command(property = ViewProperty.Button.onClick, execute = TestViewProtocol.onButton2Click)
    lateinit var button2: Button

    override fun bindView(view: View) {
        super.bindView(view)
        viewBinder.bind<String>(TestViewProtocol.getHelloWordText, { string ->
            infoText.text = string ?: ""
        })
    }
}
