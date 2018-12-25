package com.munch.lib.libnative;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Munch on 2018/12/25 23:58.
 */
public class ConvertHelper {

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T convert(@Nullable Object... objs) {
        if (null == objs || objs.length == 0) {
            throw MethodException.defaultException();
        }
        return (T) objs[0];
    }
}
