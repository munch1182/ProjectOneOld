package com.munch.module.template.app;

import android.app.Application;
import com.munhc.lib.libnative.helper.AppHelper;

/**
 * Created by Munch on 2019/7/26 16:01
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppHelper.getInstance().init(this);
    }
}
