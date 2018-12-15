package com.munch.lib.nativelib.base.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Munch on 2018/12/14.
 */
public interface IModel<T> {

    @Nullable
    T getData(@Nullable OnDataListener<T> l, @Nullable Object... parameters);

    void saveData(@Nullable T t);

    void saveData(@Nullable T t, @NonNull OnDataListener<T> l);

    interface OnDataListener<T> {

        void onDataSuccess(T t);

        void onDataFail(int code, @Nullable Object... parameters);
    }

}
