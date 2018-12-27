package com.munch.lib.app;

import android.app.Application;

import com.munch.lib.libnative.helper.AppHelper;

/**
 * Created by Munch on 2018/12/25 20:39.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppHelper.init(this);
        if (AppHelper.isDebug()) {
            ExceptionHandler.getInstance().init();
        }
    }
}
