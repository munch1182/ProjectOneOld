package com.munch.lib.libnative.helper;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

/**
 * 有些无需Context的方法需要{@link AppHelper#init(Application)}
 * <p>
 * Created by Munch on 2018/12/28 14:27.
 */
public class ResHelper {

    private ResHelper() {
    }

    public static String getStr(Context context, @StringRes int resId) {
        return context.getString(resId);
    }

    public static String getStr(@StringRes int resId) {
        return AppHelper.getApp().getString(resId);
    }

    public static int getColor(Context context, @ColorRes int resId) {
        return ContextCompat.getColor(context, resId);
    }

    public static int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(AppHelper.getApp(), resId);
    }

    public static Drawable getDrawable(Context context, @ColorRes int resId) {
        return ContextCompat.getDrawable(context, resId);
    }

    public static Drawable getDrawable(@ColorRes int resId) {
        return ContextCompat.getDrawable(AppHelper.getApp(), resId);
    }

}
