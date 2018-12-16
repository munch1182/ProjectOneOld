package com.munch.lib.nativelib.base;

import android.app.Application;
import android.os.Process;

import com.munch.lib.nativelib.helper.ActivityStackHelper;

/**
 * Created by Munch on 2018/12/16.
 */
public class BaseRootApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityStackHelper.getInstance().init(this);
    }

    public static void exit() {
        ActivityStackHelper.getInstance().finishAll();
        try {
            System.exit(0);
        } finally {
            Process.killProcess(Process.myPid());
        }
    }
}
