package com.munch.lib.expand;

import android.support.annotation.Nullable;

import com.munch.lib.libnative.helper.ObjectHelper;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 * Created by Munch on 2018/12/27 16:28.
 */
public class Loglog {

    private final static String SPLIT_STR = ",  ";

    public static void log(@Nullable Object... objs) {
        initLogger();
        if (ObjectHelper.isNull(objs)) {
            Logger.d("Null Value");
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (Object obj : objs) {
            if (ObjectHelper.isNull(obj)) {
                builder.append("Null Value");
            } else {
                builder.append(obj.toString());
            }
            builder.append(SPLIT_STR);
        }
        String msg = builder.toString();
        if (msg.endsWith(SPLIT_STR)) {
            msg = msg.substring(0, msg.lastIndexOf(SPLIT_STR));
        }
        Logger.d(msg);
    }

    private static void initLogger() {
        Logger.addLogAdapter(new AndroidLogAdapter());
    }
}
