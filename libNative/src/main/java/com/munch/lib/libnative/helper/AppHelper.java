package com.munch.lib.libnative.helper;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Process;

import com.munch.lib.libnative.exception.MethodException;

/**
 * Created by Munch on 2018/12/27 16:03.
 */
public class AppHelper {

    private static Application sApp;
    private static Boolean sIsDebug;

    private AppHelper() {
    }

    public static void setIsDebug(Boolean isDebug) {
        sIsDebug = isDebug;
    }

    /**
     * @param app Application
     * @see #getApp()
     */
    public static void init(Application app) {
        AppHelper.sApp = app;
    }

    /**
     * @return Application
     * @see #init(Application)
     */
    public static Application getApp() {
        return sApp;
    }

    /**
     * 手动设置则依照手动设置
     * <p>
     * 未手动设置debug状态时根据编译时的状态判断是否处于开发状态
     *
     * @return 未设置时当Build Variant或BuildType为release时返回true，未签名或者选择debug时返回false
     * @see #init(Application)
     * @see #setIsDebug(Boolean)
     */
    public static Boolean isDebug() {
        if (ObjectHelper.isNull(sIsDebug)) {
            try {
                ApplicationInfo info = sApp.getApplicationInfo();
                sIsDebug = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            } catch (Exception e) {
                throw MethodException.unkownException(e.getMessage());
            }
        }
        return sIsDebug;
    }


    /**
     * 退出应用，一般来说让系统退出就好，而无需调用此方法
     */
    public static void exit() {
        Process.killProcess(Process.myPid());
        System.exit(1);
    }
}
