package com.munch.lib.libnative.helper;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.munch.lib.libnative.exception.MethodException;

/**
 * Created by Munch on 2018/12/25 23:50.
 */
public interface IContextHelper {

    @Nullable
    default Context getViewContext() {
        if (this instanceof Context) {
            return (Context) this;
        } else if (this instanceof Fragment) {
            return ((Fragment) this).getActivity();
        } else {
            throw MethodException.defaultException("除Context子类和android.support.v4.app.Fragment及其子类外，此方法需要自行实现");
        }
    }
}
