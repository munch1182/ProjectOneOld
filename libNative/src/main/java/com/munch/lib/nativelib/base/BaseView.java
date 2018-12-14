package com.munch.lib.nativelib.base;

import android.content.Context;

/**
 * Created by Munch on 2018/12/14.
 */
public class BaseView<P extends BasePresenter> implements IView {

    @Override
    public Context getViewContext() {
        return null;
    }
}
