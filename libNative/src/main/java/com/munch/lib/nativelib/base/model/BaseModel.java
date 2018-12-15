package com.munch.lib.nativelib.base.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Created by Munch on 2018/12/14.
 */
public abstract class BaseModel<T> implements IModel<T> {

    @Nullable
    @Override
    public T getData(@Nullable OnDataListener<T> l, @Nullable Object... parameters) {
        return null;
    }

    @Override
    public void saveData(@Nullable T t) {
    }

    @Override
    public void saveData(@Nullable T t, @NonNull OnDataListener<T> l) {

    }
}
