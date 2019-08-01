package com.munch.module.template.mvp;

import androidx.annotation.Nullable;

/**
 * loading content error empty view
 * Created by Munch on 2019/8/1 15:04
 */
public interface LceView<P extends IPresenter> extends IView<P> {

    void showLoading(@Nullable String msg);

    void endLoadView();

    void showError(@Nullable Throwable e);

    void showEmpty();

    void showContent(@Nullable Object obj);
}
