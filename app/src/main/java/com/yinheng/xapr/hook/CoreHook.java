package com.yinheng.xapr.hook;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;

import com.yinheng.xapr.BuildConfig;
import com.yinheng.xapr.common.Collections;
import com.yinheng.xapr.common.Consumer;
import com.yinheng.xapr.common.WritableStringSetRepo;
import com.yinheng.xapr.provider.Bases;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lombok.Getter;
import lombok.Setter;

import static com.yinheng.xapr.provider.Bases.EXTRA_IGNORE_PACKAGE_CHANGED;

/**
 * Created by guohao4 on 2018/1/16.
 * Email: Tornaco@163.com
 */

@Setter
@Getter
public class CoreHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private Context context;

    private SystemGesturesPointerEventListener systemGesturesPointerEventListener;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("android".equals(lpparam.packageName)) {
            XposedLog.boot("CoreHook handleLoadPackage fro android");

            hookAMS(lpparam);

            hookForTopActivityForOreo(lpparam);

            hookGestures(lpparam);
        }
    }

    private void hookGestures(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",
                lpparam.classLoader),
                "init", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Object orig = XposedHelpers.getObjectField(param.thisObject, "mSystemGestures");
                        XposedLog.boot("CoreHook init orig: " + orig);
                    }
                });

        XposedBridge.hookAllMethods(XposedHelpers
                        .findClass("com.android.server.policy.SystemGesturesPointerEventListener",
                                lpparam.classLoader),
                "onPointerEvent", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        if (getContext() != null) {
                            if (getSystemGesturesPointerEventListener() == null) {
                                setSystemGesturesPointerEventListener(
                                        new SystemGesturesPointerEventListener(getContext(),
                                                new SystemGesturesPointerEventListenerCallbackImpl(getContext())));
                            } // End if.
                            XposedLog.verbose("top: " + getTopApp());

                            if (getTopApp() != null && !getSetRepo().has(getTopApp())) {
                                getSystemGesturesPointerEventListener().onPointerEvent((MotionEvent) param.args[0]);
                            }
                        }

                    }
                });
    }

    @Getter
    private WritableStringSetRepo setRepo;

    @Getter
    @Setter
    private String topApp;

    @Getter
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent == null) return;
                if (Bases.ACTION_IGNORE_PACKAGE_CHANGED.equals(intent.getAction())) {
                    String[] pkg = intent.getStringArrayExtra(EXTRA_IGNORE_PACKAGE_CHANGED);

                    if (pkg == null) return;

                    XposedLog.boot("ignored_packages: " + Arrays.toString(pkg));

                    Collections.consumeRemaining(pkg, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            setRepo.add(s);
                        }
                    });
                }
            } catch (Throwable e) {
                XposedLog.wtf(Log.getStackTraceString(e));
            }
        }
    };

    private void onSystemReady() {
        try {
            this.setRepo = Bases.newWritableRepo(Bases.getIgnoreFile());

            if (BuildConfig.DEBUG) {
                this.setRepo.add("com.crdroid.home");
            }

            XposedLog.boot("stringSetRepo: " + setRepo);

            IntentFilter intentFilter = new IntentFilter(Bases.ACTION_IGNORE_PACKAGE_CHANGED);
            getContext().registerReceiver(getBroadcastReceiver(), intentFilter);
        } catch (Throwable e) {
            XposedLog.wtf("onSystemReady: " + e);
        }
    }

    private void hookAMS(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("CoreHook hookAMSStart...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "start", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    setContext(context);
                    XposedLog.boot("CoreHook start context: " + context);
                }
            });

            XposedBridge.hookAllMethods(ams, "systemReady", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedLog.boot("systemReady" + context);
                    onSystemReady();
                }
            });
        } catch (Exception e) {
            XposedLog.boot(Log.getStackTraceString(e));
        }
    }

    private void hookForTopActivityForOreo(XC_LoadPackage.LoadPackageParam lpparam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityStackSupervisor",
                    lpparam.classLoader);
            XposedBridge.hookAllMethods(ams, "setFocusStackUnchecked", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String reason = (String) param.args[0];
                    Object activityStack = param.args[1];
                    Object taskRecord = XposedHelpers.callMethod(activityStack, "topTask");
                    if (taskRecord == null) return;
                    Object realActivityObj = XposedHelpers.getObjectField(taskRecord, "realActivity");
                    if (realActivityObj != null) {
                        ComponentName componentName = (ComponentName) realActivityObj;
                        XposedLog.verbose("setFocusStackUnchecked:" + componentName);
                        setTopApp(componentName.getPackageName());
                    } else {
                        XposedLog.verbose("setFocusStackUnchecked, no realActivity obj");
                    }
                }
            });
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }
}
