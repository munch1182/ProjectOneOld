package com.munch.lib.nativelib.base.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.nativelib.base.model.IModel;
import com.munch.lib.nativelib.base.view.IView;
import com.munch.lib.nativelib.constant.C;
import com.munch.lib.nativelib.helper.ObjectHelper;

import java.lang.ref.WeakReference;

/**
 * Created by Munch on 2018/12/14.
 */
public abstract class BasePresenter<T,V extends IView, M extends IModel<T>>
        implements IPresenter {

    private WeakReference<V> mRefView;
    private M m;

    /**
     * @throws RuntimeException 绑定之前调用了此方法
     * @see BasePresenter#takeView(IView)
     * @see BasePresenter#manageView(IView, android.arch.lifecycle.LifecycleOwner)
     */
    @Nullable
    public V getView() throws RuntimeException {
        if (ObjectHelper.isEmpty(mRefView)) {
            throw new RuntimeException(C.Exception.ERROR_DO_WRONG_TIME);
        }
        return mRefView.get();
    }

    @Override
    public void start() {
    }

    @Override
    public void start(int type) {
    }

    @Override
    public void start(int type, @Nullable Object... parameters) {
    }

    @Override
    public void modifyData(int type, @Nullable Object... parameters) {
        m.saveData(dataAdapter(type,parameters));
    }

    /**
     * 将参数转换为bean类
     */
    public T dataAdapter(int type, @Nullable Object... parameters) {
        return null;
    }


    @NonNull
    public M getModel() {
        return m;
    }

    @NonNull
    public M putModel(@NonNull M m) {
        this.m = m;
        return this.m;
    }

    @NonNull
    public V getNonNullView() throws RuntimeException {
        if (ObjectHelper.haveEmpty(mRefView, mRefView.get())) {
            throw new RuntimeException(C.Exception.ERROR_DO_WRONG_TIME);
        }
        return mRefView.get();
    }

    public BasePresenter<T,V, M> manageView(@NonNull V v, @NonNull LifecycleOwner l) {
        l.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy() {
                dropView();
            }
        });
        return takeView(v);
    }

    public BasePresenter<T,V, M> takeView(@NonNull V v) {
        mRefView = new WeakReference<>(v);
        return this;
    }

    public void dropView() {
        if (ObjectHelper.isNonEmpty(mRefView)) {
            mRefView.clear();
        }
    }
}
