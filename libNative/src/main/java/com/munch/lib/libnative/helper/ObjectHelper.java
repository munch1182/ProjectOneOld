package com.munch.lib.libnative.helper;

import android.support.annotation.Nullable;

import java.util.Collection;

/**
 * Created by Munch on 2018/12/26 13:46.
 */
public class ObjectHelper {

    private ObjectHelper() {
    }

    public static boolean isNullOrEmpty(@Nullable Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNull(@Nullable Object obj) {
        return null == obj;
    }

    public static boolean isNullOrEmpty(@Nullable Object[] objects) {
        return objects == null || objects.length == 0;
    }
}
