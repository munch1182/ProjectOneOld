package com.munch.lib.nativelib.base;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.nativelib.constant.C;
import com.munch.lib.nativelib.helper.ObjectHelper;

import java.lang.ref.WeakReference;

/**
 * Created by Munch on 2018/12/14.
 */
public class BasePresenter<V extends IView, M extends IModel>
        implements IPresenter<V, M> {

    private WeakReference<V> mRefView;
    private M m;

    @Override
    public void start() {
    }

    @Nullable
    @Override
    public V getView() {
        if (ObjectHelper.isEmpty(mRefView)) {
            return null;
        }
        return mRefView.get();
    }

    @Nullable
    @Override
    public M getModel() {
        return m;
    }

    @NonNull
    @Override
    public V getNonNullView() throws RuntimeException {
        V view = getView();
        if (ObjectHelper.isEmpty(view)) {
            throw new RuntimeException(C.Exception.ERROR_NULL_POINT);
        }
        return view;
    }

    @NonNull
    @Override
    public M getNonNullModel() throws RuntimeException {
        M m = getModel();
        if (ObjectHelper.isEmpty(m)) {
            throw new RuntimeException(C.Exception.ERROR_NULL_POINT);
        }
        return m;
    }

    @Override
    public BasePresenter<V, M> manageView(V v, Lifecycle l) {
        takeView(v);
        l.addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy() {
                dropView();
            }
        });
        return this;
    }

    @Override
    public BasePresenter<V, M> takeView(V v) {
        mRefView = new WeakReference<>(v);
        return this;
    }

    @Override
    public void dropView() {
        if (ObjectHelper.isNotEmpty(mRefView)) {
            mRefView.clear();
        }
    }
}
