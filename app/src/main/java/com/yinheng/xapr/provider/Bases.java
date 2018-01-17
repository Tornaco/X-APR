package com.yinheng.xapr.provider;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;

import com.yinheng.xapr.common.WritableStringSetRepo;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guohao4 on 2018/1/17.
 * Email: Tornaco@163.com
 */

public class Bases {

    public static final String ACTION_IGNORE_PACKAGE_CHANGED = "tornaco.action.apr.ignore_package";
    public static final String EXTRA_IGNORE_PACKAGE_CHANGED = "ignored_packagese";

    private static final File IGNORE_FILE =
            new File("data/system/apr/config_ignored_packages");

    public static File getIgnoreFile() {
        return IGNORE_FILE;
    }

    public static WritableStringSetRepo newWritableRepo(File file) {
        ExecutorService exe = Executors.newSingleThreadExecutor();
        HandlerThread hr = new HandlerThread("APR-W" + file);
        hr.start();
        Handler h = new Handler(hr.getLooper());
        return new WritableStringSetRepo(file, h, exe);
    }

    public static void sendPackageIgnoreBroadcast(Context context, String... pkgs) {
        Intent i = new Intent(ACTION_IGNORE_PACKAGE_CHANGED);
        i.putExtra(EXTRA_IGNORE_PACKAGE_CHANGED, pkgs);
        context.sendBroadcast(i);
    }
}
