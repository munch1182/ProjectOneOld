package com.munch.lib.nativelib.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by Munch on 2018/12/16.
 */
public interface IViewExpand {

    void start();

    default void initListener() {
    }

    default View getLayoutView(@Nullable Bundle bundle) {
        return null;
    }

    @LayoutRes
    default int getLayoutResId(@Nullable Bundle bundle) {
        return 0;
    }


}
