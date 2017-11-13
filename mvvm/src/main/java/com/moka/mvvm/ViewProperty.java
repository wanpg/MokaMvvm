package com.moka.mvvm;

/**
 * Created by wangjinpeng on 2017/11/13.
 */

public class ViewProperty {

    public static class View {
        public static final String enabled = "enabled";
        public static final String onClick = "onClick";
    }

    public static class TextView extends View {
        public static final String text = "text";
    }

    public static class Button extends TextView {

    }
}
/*
public class ViewProperty {

    public static class View {
        public static final String enabled = "enabled";
        public static final String onClick = "onClick";
    }

    public static class TextView extends View {
        public static final String text = "text";
    }

    public static class Button extends TextView {

    }

}
*/

