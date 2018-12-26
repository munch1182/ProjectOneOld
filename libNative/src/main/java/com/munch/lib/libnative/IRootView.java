package com.munch.lib.libnative;

import android.content.Context;
import android.support.annotation.Nullable;

import com.munch.lib.libnative.helper.IContextHelper;

/**
 * Created by Munch on 2018/12/25 23:48.
 */
public interface IRootView<T> extends IContextHelper {
    @Nullable
    @Override
    default Context getViewContext() {
        return null;
    }

}
