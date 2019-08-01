package com.munch.module.template.mvp;

import androidx.annotation.Nullable;

/**
 * Created by Munch on 2019/7/29 17:13
 */
public interface IPresenter<V extends IView> {

    V attachView(V v);

    void detachView();

    @Nullable
    V getView();

    boolean isViewAttach();
}
