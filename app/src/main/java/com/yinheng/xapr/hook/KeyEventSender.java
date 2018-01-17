package com.yinheng.xapr.hook;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import de.robv.android.xposed.XposedHelpers;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */

class KeyEventSender {

    static boolean injectInputEvent(InputEvent event, int mode) {
        XposedLog.boot("injectInputEvent: " + event);
        InputManager inputManager = InputManager.getInstance();
        try {
            return (boolean) XposedHelpers.callMethod(inputManager,
                    "injectInputEvent", event, mode);
        } catch (Throwable e) {
            XposedLog.boot("Fail injectInputEvent: " + event
                    + ", error: " + Log.getStackTraceString(e));
            return false;
        }
    }

    private static boolean injectKey(int code) {
        int flags = KeyEvent.FLAG_FROM_SYSTEM;
        final long eventTime = SystemClock.uptimeMillis();
        KeyEvent down = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, code, 0,
                0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, flags, InputDevice.SOURCE_UNKNOWN);
        KeyEvent up = new KeyEvent(eventTime + 10, eventTime + 10, KeyEvent.ACTION_UP, code, 0,
                0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, flags, InputDevice.SOURCE_UNKNOWN);
        return injectInputEvent(down, 0) && injectInputEvent(up, 0);
    }

    static boolean injectHomeKey() {
        return injectKey(KeyEvent.KEYCODE_HOME);
    }

    static boolean injectBackKey() {
        return injectKey(KeyEvent.KEYCODE_BACK);
    }

    static boolean injectRecentKey() {
        return injectKey(KeyEvent.KEYCODE_APP_SWITCH);
    }
}
