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
}
