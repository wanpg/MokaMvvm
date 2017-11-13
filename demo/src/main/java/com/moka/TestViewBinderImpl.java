package com.moka;

import android.view.View;

import com.moka.mvvm.Observable;
import com.moka.mvvm.ViewBinder;
import com.moka.mvvm.ViewController;
import com.moka.mvvm.ViewModel;
import com.moka.mvvm.utils.BindUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Created by wangjinpeng on 2017/11/10.
 */

public class TestViewBinderImpl extends ViewBinder implements TestViewProtocol {

    private TestViewController viewController;
    private TestViewModel viewModel;

    public TestViewBinderImpl(TestViewController viewController) {
        this.viewController = viewController;
    }

    @Override
    public void setViewModel(@NotNull ViewModel viewModel) {
        this.viewModel = (TestViewModel) viewModel;
    }

    @Override
    public void initView(View container) {
        viewController.infoText = container.findViewById(R.id.hello_world);
        viewController.button1 = container.findViewById(R.id.button1);
        viewController.button2 = container.findViewById(R.id.button2);
    }

    @Override
    public void dataBind() {
        BindUtils.INSTANCE.bind(getButtonEnable(), new Function1<Boolean, Unit>() {
            @Override
            public Unit invoke(Boolean aBoolean) {
                viewController.button1.setEnabled(aBoolean);
                return null;
            }
        });
        BindUtils.INSTANCE.bind(getButtonText(), new Function1<String, Unit>() {
            @Override
            public Unit invoke(String s) {
                viewController.button1.setText(s);
                return null;
            }
        });
        BindUtils.INSTANCE.bind(getHelloWordText(), new Function1<String, Unit>() {
            @Override
            public Unit invoke(String s) {
                viewController.infoText.setText(s);
                return null;
            }
        });
//        viewController.getButton2().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onButton2Click();
//            }
//        });
    }

    @NotNull
    @Override
    public ViewController getViewController() {
        return viewController;
    }

    @Override
    public void executeCommand(@NotNull String command, @NotNull Object... args) {
        if (TestViewProtocol.onButton2Click.equals(command)) {
            onButton2Click(args.length > 0 ? (View) args[0] : null);
        } else {
            super.executeCommand(command, args);
        }
    }

    @NotNull
    @Override
    public Observable<String> getHelloWordText() {
        final Observable<String> observable = new Observable<>();
        viewModel.getInfo().addObserver(new Function1<String, Unit>() {
            @Override
            public Unit invoke(String s) {
                observable.set(s);
                return null;
            }
        });
        return observable;
    }

    @NotNull
    @Override
    public Observable<String> getButtonText() {
        final Observable<String> observable = new Observable<>();
        viewModel.getButtonInfo().addObserver(new Function1<ButtonInfo, Unit>() {
            @Override
            public Unit invoke(ButtonInfo buttonInfo) {
                observable.set(buttonInfo.getText());
                return null;
            }
        });
        return observable;
    }

    @NotNull
    @Override
    public Observable<Boolean> getButtonEnable() {
        final Observable<Boolean> observable = new Observable<>();
        viewModel.getButtonInfo().addObserver(new Function1<ButtonInfo, Unit>() {
            @Override
            public Unit invoke(ButtonInfo buttonInfo) {
                observable.set(buttonInfo.isEnable());
                return null;
            }
        });
        return observable;
    }

    @Override
    public void onButton2Click(@Nullable View view) {
        viewModel.onButton2Click(view);
    }
}
