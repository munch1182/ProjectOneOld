package com.munch.module.template.mvp;

import androidx.annotation.Nullable;
import com.munch.lib.libnative.root.RootFragment;
import com.munch.module.template.rxjava.DisposableManager;

/**
 * Created by Munch on 2019/8/1 15:27
 */
public class BaseMvpFragment<P extends IPresenter> extends RootFragment implements LceView<P> {

    private P p;
    private DisposableManager mManager;

    @Override
    @SuppressWarnings("unchecked")
    public P bindPresenter(P p) {
        this.p = p;
        this.p.attachView(this);
        return this.p;
    }

    @Nullable
    @Override
    public P getPresenter() {
        return p;
    }

    @Override
    public DisposableManager getManager() {
        if (null == mManager) {
            mManager = DisposableManager.getInstance();
        }
        return mManager;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.p != null) {
            this.p.detachView();
        }
        if (null != mManager) {
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
