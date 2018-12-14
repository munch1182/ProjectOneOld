package com.munch.lib.nativelib.helper;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Munch on 2018/12/13.
 */
public class ConvertHelper {

    public static int px2Dp(Context context, float pxValue) {
        return (int) (pxValue / (context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int dp2Px(Context context, float dpValue) {
        return (int) (dpValue * (context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static Date date2Str(String val, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        try {
            return dateFormat.parse(val);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
