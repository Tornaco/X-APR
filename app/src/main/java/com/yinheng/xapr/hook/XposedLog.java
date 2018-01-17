package com.yinheng.xapr.hook;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by guohao4 on 2018/1/16.
 * Email: Tornaco@163.com
 */

public class XposedLog {

    public static void boot(String format, Object... args) {
        XposedBridge.log("X-APR-" + String.format(format, args));
    }


    public static void wtf(String format, Object... args) {
        XposedBridge.log("X-APR-WTF-" + String.format(format, args));
    }

    public static void verbose(String format, Object... args) {
        XposedBridge.log("X-APR-V-" + String.format(format, args));
    }
}
