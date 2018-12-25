package com.munch.lib.libnative;

import android.support.annotation.Nullable;

/**
 * Created by Munch on 2018/12/25 23:48.
 */
public interface IRootView extends IContextHelper {

    default void syncViewByType(int type, @Nullable Object... objs) {
    }
}
