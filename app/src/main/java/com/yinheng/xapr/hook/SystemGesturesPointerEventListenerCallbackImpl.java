package com.yinheng.xapr.hook;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by guohao4 on 2018/1/16.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@Getter
public class SystemGesturesPointerEventListenerCallbackImpl
        implements SystemGesturesPointerEventListener
        .Callbacks {

    @Override
    public void onSwipeFromTop() {
        XposedLog.boot("onSwipeFromTop");
    }

    @Override
    public void onSwipeFromBottom(int x) {
        XposedLog.boot("onSwipeFromBottom: " + x);
    }

    @Override
    public void onSwipeFromRight() {
        XposedLog.boot("onSwipeFromRight");
    }

    @Override
    public void onSwipeFromLeft() {
        XposedLog.boot("onSwipeFromLeft");
    }

    @Override
    public void onFling(int durationMs) {
        XposedLog.boot("onFling");
    }

    @Override
    public void onDown() {
        XposedLog.boot("onDown");
    }

    @Override
    public void onUpOrCancel() {
        XposedLog.boot("onUpOrCancel");
    }

    @Override
    public void onMouseHoverAtTop() {

    }

    @Override
    public void onMouseHoverAtBottom() {

    }

    @Override
    public void onMouseLeaveFromEdge() {

    }

    @Override
    public void onDebug() {

    }
}
