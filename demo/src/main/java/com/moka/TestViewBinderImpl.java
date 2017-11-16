//package com.moka;
//
//import android.view.View;
//
//import com.moka.mvvm.ViewBinder;
//import com.moka.mvvm.ViewController;
//import com.moka.mvvm.ViewModel;
//
//import org.jetbrains.annotations.NotNull;
//
//import kotlin.Unit;
//import kotlin.jvm.functions.Function1;
//
///**
// * Created by wangjinpeng on 2017/11/10.
// */
//
//
//public class TestViewBinderImpl extends ViewBinder {
//
//    private TestViewController viewController;
//    private TestViewModel viewModel;
//
//    public TestViewBinderImpl(TestViewController viewController) {
//        this.viewController = viewController;
//    }
//
//    @Override
//    public void setViewModel(@NotNull ViewModel viewModel) {
//        this.viewModel = (TestViewModel) viewModel;
//    }
//
//    @Override
//    public void initView(View container) {
//        viewController.infoText = container.findViewById(R.id.hello_world);
//        viewController.button1 = container.findViewById(R.id.button1);
//        viewController.button2 = container.findViewById(R.id.button2);
//    }
//
//    @Override
//    public void dataBind() {
//        viewModel.getButtonInfo().addObserver(new Function1<ButtonInfo, Unit>() {
//            @Override
//            public Unit invoke(ButtonInfo buttonInfo) {
//                viewModel.getButtonEnable();
//                viewModel.getButtonText();
//                return null;
//            }
//        });
//
//        viewModel.getInfo().addObserver(new Function1<String, Unit>() {
//            @Override
//            public Unit invoke(String s) {
//                viewModel.getHelloWordText();
//                return null;
//            }
//        });
//
//        viewModel.getHelloWordText().addObserver(new Function1<String, Unit>() {
//            @Override
//            public Unit invoke(String s) {
//                viewController.infoText.setText(s);
//                return null;
//            }
//        });
//
//        viewModel.getButtonText().addObserver(new Function1<String, Unit>() {
//            @Override
//            public Unit invoke(String s) {
//                viewController.button1.setText(s);
//                return null;
//            }
//        });
//
//        viewModel.getButtonEnable().addObserver(new Function1<Boolean, Unit>() {
//            @Override
//            public Unit invoke(Boolean aBoolean) {
//                viewController.button1.setEnabled(aBoolean);
//                return null;
//            }
//        });
//
//        viewController.getButton2().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onButton2Click();
//            }
//        });
//    }
//
//    @Override
//    public void executeCommand(@NotNull String command, @NotNull Object... args) {
//        if (TestViewProtocol.onButton2Click.equals(command)) {
//            viewModel.onButton2Click(args.length > 0 ? (View) args[0] : null);
//        } else {
//            super.executeCommand(command, args);
//        }
//    }
//}
