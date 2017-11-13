package com.moka

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window

class MainActivity : AppCompatActivity() {

    internal lateinit var testViewController: TestViewController
    internal lateinit var testViewModel: TestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testViewController = TestViewController(this, findViewById<View>(Window.ID_ANDROID_CONTENT))
        testViewModel = TestViewModel(this)
        testViewController.setViewModel(testViewModel)

        lifecycle.addObserver(testViewController)
        lifecycle.addObserver(testViewModel)
    }
}
