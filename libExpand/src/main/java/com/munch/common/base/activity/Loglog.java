package com.munch.common.base.activity;

import com.munch.lib.nativelib.helper.AppHelper;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 * Created by Munch on 2018/12/16.
 */
public class Loglog {

    public static void log(Object obj) {
        if (!AppHelper.isDebug()) {
            return;
        }
        Logger.addLogAdapter(new AndroidLogAdapter());
        Logger.d(obj);
    }
}
