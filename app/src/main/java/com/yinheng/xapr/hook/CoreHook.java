package com.yinheng.xapr.hook;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lombok.Getter;
import lombok.Setter;

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

                            XposedLog.boot("CoreHook onPointerEvent");

                            if (getContext() != null) {
                                if (getSystemGesturesPointerEventListener() == null) {
                                    setSystemGesturesPointerEventListener(
                                            new SystemGesturesPointerEventListener(getContext(),
                                                    new SystemGesturesPointerEventListenerCallbackImpl()));
                                } // End if.
                                param.setResult(null);
                                getSystemGesturesPointerEventListener().onPointerEvent((MotionEvent) param.args[0]);
                            }

                        }
                    });
        }
    }

    private void hookAMS(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("CoreHook hookAMSStart...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "start", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    setContext(context);
                    XposedLog.boot("CoreHook start context: " + context);
                }
            });
        } catch (Exception e) {
            XposedLog.boot(Log.getStackTraceString(e));
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }
}
