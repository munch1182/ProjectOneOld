package com.munch.lib.nativelib.helper;

import android.app.Application;

/**
 * Created by Munch on 2018/12/17.
 */
public class AppHelper {

    private static Application mApplication;
    private static boolean isDebug = false;

    /**
     * @see #getApplication()
     */
    public static void init(Application application) {
        mApplication = application;
    }

    /**
     * @see #init(Application)
     */
    public static Application getApplication() {
        return mApplication;
    }

    public static boolean isDebug() {
        return isDebug;
    }


    public static void isDebug(boolean isDebug) {
        AppHelper.isDebug = isDebug;
    }
}
