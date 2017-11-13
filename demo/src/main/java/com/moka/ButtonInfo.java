package com.moka;

/**
 * Created by wangjinpeng on 2017/11/13.
 */

public class ButtonInfo {

    private String text;
    private boolean enable;

    public ButtonInfo(String text, boolean enable) {
        this.text = text;
        this.enable = enable;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}