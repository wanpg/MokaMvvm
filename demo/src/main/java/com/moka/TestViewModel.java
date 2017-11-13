package com.moka;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.moka.annotations.BindTo;
import com.moka.annotations.BindsTo;
import com.moka.mvvm.Observable;
import com.moka.mvvm.ViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * Created by wangjinpeng on 2017/11/9.
 */

public class TestViewModel extends ViewModel {

    private Handler handler = new Handler();

    @BindTo(TestViewProtocol.helloWordText)
    private Observable<String> info = new Observable<>("hello world !!!!!!");

    @BindsTo({
            @BindTo(value = TestViewProtocol.buttonEnable, field = "enable"),
            @BindTo(value = TestViewProtocol.buttonText, field = "text")
    })
    private Observable<ButtonInfo> buttonInfo = new Observable<>();

    public TestViewModel(@NotNull Context context) {
        super(context);
    }

    @BindTo(TestViewProtocol.onButton2Click)
    public void onButton2Click(View view) {
        Toast.makeText(getContext(), "点击了button2", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                info.set("和理论绿绿绿绿绿绿绿绿绿绿");
                buttonInfo.set(new ButtonInfo("延时设置的button文案", false));
            }
        }, 2000);
    }

    public Observable<String> getInfo() {
        return info;
    }

    public Observable<ButtonInfo> getButtonInfo() {
        return buttonInfo;
    }
}
