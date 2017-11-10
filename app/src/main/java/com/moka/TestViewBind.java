package com.moka;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.moka.mvvm.ViewBind;
import com.moka.mvvm.annotation.Bind;

import org.jetbrains.annotations.NotNull;

/**
 * Created by wangjinpeng on 2017/11/9.
 */

public class TestViewBind extends ViewBind {

    @Bind(viewId = R.id.hello_world, bindProp = "info")
    TextView infoText;

    public TestViewBind(@NotNull View container) {
        super(container);
    }

    @Override
    public void bindView(@NonNull View view) {
    }
}
