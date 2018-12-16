package com.munch.lib.nativelib.rootmvp;

import android.support.annotation.NonNull;

import com.munch.lib.nativelib.mpvpack.IView;
import com.munch.lib.nativelib.mpvpack.IBasePresenter;

/**
 * 此页面可以复制在自己的view中实现以避免多继承
 * <p>
 * Created by Munch on 2018/12/14.
 */
public abstract class BaseRootView<T, P extends IBasePresenter> implements IView<T> {

    private P p;

    public P bindPresenter(@NonNull P p) {
        this.p = p;
        return this.p;
    }

    @NonNull
    public P getPresenter() {
        return p;
    }
}
