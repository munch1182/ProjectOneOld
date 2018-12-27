package com.munch.lib.libnative.helper;

import android.content.Context;

/**
 * Created by Munch on 2018/12/25 23:58.
 */
public class ConvertHelper {

    private ConvertHelper() {
    }

    public static int px2Dp(Context context, float pxValue) {
        return (int) (pxValue / context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int dp2Px(Context context, float dpValue) {
        return (int) (dpValue * context.getResources().getDisplayMetrics().density + 0.5f);
    }

}
