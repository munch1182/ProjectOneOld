package com.munch.lib.nativelib.base;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Munch on 2018/12/15.
 */
public interface IPresenter<V extends IView, M extends IModel> {

    /**
     * presenter的入口
     */
    void start();

    @Nullable
    V getView();

    @Nullable
    M getModel();

    @NonNull
    V getNonNullView() throws RuntimeException;

    /**
     * 只有在肯定不为空时才能调用此方法
     *
     * @return 获取Model的引用，只能在不为空时使用
     */
    @NonNull
    M getNonNullModel() throws RuntimeException;

    /**
     * 持有View的引用并自动销毁
     */
    IPresenter<V, M> manageView(V v, Lifecycle l);

    /**
     * 引用View
     */
    IPresenter<V, M> takeView(V v);

    /**
     * 释放View
     */
    void dropView();

}
