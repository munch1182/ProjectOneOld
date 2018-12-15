package com.munch.lib.nativelib.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.munch.lib.nativelib.base.presenter.IPresenter;
import com.munch.lib.nativelib.base.view.IView;

/**
 * Created by Munch on 2018/12/16.
 */
public class BaseActivity <T, P extends IPresenter> extends AppCompatActivity implements IView<T> {

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