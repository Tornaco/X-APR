package com.yinheng.xapr.hook;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import lombok.Getter;

/**
 * Created by guohao4 on 2018/1/16.
 * Email: Tornaco@163.com
 */
@Getter
public class SystemGesturesPointerEventListenerCallbackImpl
        implements SystemGesturesPointerEventListener
        .Callbacks {

    private int screenWidth, screenHeight;

    private Context context;

    SystemGesturesPointerEventListenerCallbackImpl(Context context) {
        this.context = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        screenWidth = display.getWidth();  // deprecated
        screenHeight = display.getHeight();  // deprecated
    }

    SwipeAreaX getSwipeAreaX(int x) {
        if (x <= screenWidth / 4) return SwipeAreaX.LEFT;
        if (x >= screenWidth - screenWidth / 4) return SwipeAreaX.RIGHT;
        return SwipeAreaX.CENTER;
    }

    SwipeAreaY getSwipeAreaY(int y) {
        if (y <= screenHeight / 4) return SwipeAreaY.TOP;
        if (y >= screenHeight - screenHeight / 4) return SwipeAreaY.BOTTOM;
        return SwipeAreaY.CENTER;
    }

    @Override
    public void onSwipeFromTop() {
        XposedLog.boot("onSwipeFromTop");
    }

    @Override
    public void onSwipeFromBottom(int x, int y) {
        SwipeAreaX sax = getSwipeAreaX(x);
        SwipeAreaY say = getSwipeAreaY(y);
        XposedLog.boot("onSwipeFromBottom: %s-%s %s-%s", x, y, sax, say);

        if (say == SwipeAreaY.BOTTOM) {
            switch (sax) {
                case LEFT:
                    KeyEventSender.injectBackKey();
                    break;
                case CENTER:
                    KeyEventSender.injectHomeKey();
                    break;
                case RIGHT:
                    KeyEventSender.injectRecentKey();
                    break;
            }
        }
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
