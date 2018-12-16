package com.munch.lib.nativelib.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Munch on 2018/12/13.
 */
public class StringHelper {


    /**
     * 保证传入的String类型不为空
     */
    @NonNull
    public static String reqNotNull(@Nullable String val) {
        if (null == val) {
            return "";
        }
        return val;
    }

    /**
     * @return 传入的String参数为null，为空或者为字符串时#Empty返回true
     */
    public static boolean isEmpty(String val) {
        return null == val || val.isEmpty();
    }

    /**
     * 当检查字符是否相同时避免空指针,不区分大小写
     */
    public static boolean checkStrIgnoreCase(String val,String val2) {
        return isEmpty(val) || val.equalsIgnoreCase(val2);
    }

    public static boolean checkStr(String val,String val2) {
        return isEmpty(val) || val.equals(val2);
    }

    public static boolean isNotEmpty(String val) {
        return !isEmpty(val);
    }



}
