package com.moka;

import android.view.View;

import com.moka.mvvm.Binder;
import com.moka.mvvm.ViewBind;

import org.jetbrains.annotations.NotNull;

/**
 * Created by wangjinpeng on 2017/11/10.
 */

public class TestViewBindBinderImpl extends Binder {

    private TestViewBind testViewBind;

    public TestViewBindBinderImpl(TestViewBind testViewBind) {
        this.testViewBind = testViewBind;
    }

    @Override
    public void initView(View container) {
        testViewBind.infoText = container.findViewById(R.id.hello_world);
    }

    @Override
    public void dataBind() {
        testViewBind.viewModel.bindText(testViewBind.infoText, "info");
    }

    @NotNull
    @Override
    public ViewBind getViewBind() {
        return testViewBind;
    }
}
