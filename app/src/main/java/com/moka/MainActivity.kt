package com.moka

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window

class MainActivity : AppCompatActivity() {

    internal lateinit var testViewBind: TestViewBind
    internal lateinit var testViewModel: TestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testViewBind = TestViewBind(findViewById<View>(Window.ID_ANDROID_CONTENT))
        testViewModel = TestViewModel()
        testViewBind.viewModel = testViewModel

        lifecycle.addObserver(testViewBind)
        lifecycle.addObserver(testViewModel)
    }
}
