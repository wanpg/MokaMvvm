package com.moka;

import android.os.Handler;

import com.moka.mvvm.Observable;
import com.moka.mvvm.ViewModel;

/**
 * Created by wangjinpeng on 2017/11/9.
 */

public class TestViewModel extends ViewModel {

    private Handler handler = new Handler();

    private Observable<String> info = new Observable<>("hello world !!!!!!");

    public void setInfo(String info) {
        this.info.set(info);
    }

    public String getInfo() {
        return info.get();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                info.set("和理论绿绿绿绿绿绿绿绿绿绿");
            }
        }, 2000);
    }
}
