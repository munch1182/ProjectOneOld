package com.munch.lib.libnative;

import android.support.annotation.Nullable;

/**
 * Created by Munch on 2018/12/26 8:51.
 */
public interface IRootPresenter {

    default void updateData(int type, @Nullable Object... objs) {
    }
}
