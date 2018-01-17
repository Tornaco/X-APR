package com.yinheng.xapr.common;

import android.util.Log;

/**
 * Created by guohao4 on 2018/1/16.
 * Email: Tornaco@163.com
 */

public class AppLog {

    public static void boot(String format, Object... args) {
        Log.d("", "X-APR-" + String.format(format, args));
    }


    public static void wtf(String format, Object... args) {
        Log.d("", "X-APR-WTF-" + String.format(format, args));
    }

    public static void verbose(String format, Object... args) {
        Log.d("", "X-APR-V-" + String.format(format, args));
    }
}
