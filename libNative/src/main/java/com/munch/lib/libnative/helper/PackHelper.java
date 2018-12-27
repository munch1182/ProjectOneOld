package com.munch.lib.libnative.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Created by Munch on 2018/12/27 19:15.
 */
public class PackHelper {

    private PackHelper() {
    }

    public static String getPackName(Context context) {
        return context.getPackageName();
    }

    public static int getVersionCode(Context context) throws PackageManager.NameNotFoundException {
        return getPackageInfo(context).versionCode;
    }

    public static String getVersionName(Context context) throws PackageManager.NameNotFoundException {
        return getPackageInfo(context).versionName;

    }

    public static String getAppName(Context context) {
        try {
            return context.getString(getPackageInfo(context)
                    .applicationInfo.labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PackageInfo getPackageInfo(Context context) throws PackageManager.NameNotFoundException {
        return context.getPackageManager().getPackageInfo(getPackName(context), PackageManager.GET_CONFIGURATIONS);
    }

    public static Drawable getAppIcon(Context context) throws PackageManager.NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        ApplicationInfo applicationInfo = manager.getApplicationInfo(getPackName(context), 0);
        return manager.getApplicationIcon(applicationInfo);
    }
}
