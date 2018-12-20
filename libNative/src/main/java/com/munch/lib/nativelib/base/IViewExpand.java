package com.munch.lib.nativelib.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;


public interface IViewExpand {

    default void initialize(Context context) {
    }

    default void findView(View rootView, Bundle bundle) {
    }

    default void initListener() {
    }

    void start();

    default View getLayoutView(@Nullable Bundle bundle) {
        return null;
    }

    @LayoutRes
    default int getLayoutResId(@Nullable Bundle bundle) {
        return 0;
    }


}
