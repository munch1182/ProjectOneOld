package com.munch.module.template.mvp;

import android.content.Context;
import androidx.annotation.Nullable;
import com.munch.lib.libnative.root.RootActivity;
import com.munch.module.template.rxjava.DisposableManager;
import com.munch.module.template.rxjava.IDisposableManager;

/**
 * Created by Munch on 2019/7/29 17:18
 */
public abstract class BaseMvpActivity<P extends IPresenter> extends RootActivity implements LceView<P> {

    private P p;
    private DisposableManager mManager;

    @Override
    @SuppressWarnings("unchecked")
    public P bindPresenter(P p) {
        this.p = p;
        this.p.attachView(this);
        return this.p;
    }

    @Override
    public IDisposableManager getManager() {
        if (null == mManager) {
            mManager = DisposableManager.getInstance();
        }
        return mManager;
    }

    @Nullable
    @Override
    public P getPresenter() {
        return p;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.p != null) {
            this.p.detachView();
        }
        if (mManager != null) {
            mManager.clear();
        }
    }

    @Override
    public void showLoading(@Nullable String msg) {

    }

    @Override
    public void endLoadView() {

    }

    @Override
    public void showError(@Nullable Throwable e) {

    }

    @Override
    public void showEmpty() {

    }

    @Override
    public void showContent(@Nullable Object obj) {

    }
}
