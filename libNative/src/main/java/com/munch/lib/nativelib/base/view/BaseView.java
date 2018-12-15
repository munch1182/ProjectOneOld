package com.munch.lib.nativelib.base.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.nativelib.base.Type;
import com.munch.lib.nativelib.base.presenter.IPresenter;

/**
 * 此页面在自己的view中实现以避免多继承
 *
 * Created by Munch on 2018/12/14.
 */
public abstract class BaseView<T, P extends IPresenter> implements IView<T> {

    private P p;

    @Nullable
    @Override
    public Context getViewContext() {
        return null;
    }

    @Override
    public void loadData(@Nullable T t) {
    }

    @Override
    public void loadDataFail(@Nullable Object... parameters) {
        syncView(Type.TYPE_LOAD_FAIL, parameters);
    }

    @Override
    public void syncView(int type, @Nullable Object... parameters) {
    }


    @SuppressWarnings("unchecked")
    public P bindPresenter(@NonNull IPresenter p) {
        this.p = (P) p;
        return this.p;
    }

    @NonNull
    public P getPresenter() {
        return p;
    }
}
