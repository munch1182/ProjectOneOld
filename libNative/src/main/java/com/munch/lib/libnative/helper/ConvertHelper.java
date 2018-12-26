package com.munch.lib.libnative.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.libnative.MethodException;

/**
 * Created by Munch on 2018/12/25 23:58.
 */
public class ConvertHelper {

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T convert(@Nullable Object... objs) {
        if (ObjectHelper.isNullOrEmpty(objs)) {
            throw MethodException.defaultException();
        }
        return (T) objs[0];
    }
}
