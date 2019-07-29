package com.munch.module.template.mvp;

import android.content.Context;
import androidx.annotation.Nullable;
import com.munch.module.template.rxjava.DisposableManager;
import com.munhc.lib.libnative.root.RootActivity;

/**
 * Created by Munch on 2019/7/29 17:18
 */
public abstract class BaseMvpActivity<P extends IPresenter> extends RootActivity implements IView<P> {

    private P p;
    private DisposableManager mManager;

    @Override
    @SuppressWarnings("unchecked")
    public P bindPresenter(P p) {
        this.p = p;
        this.p.takeView(this);
        return this.p;
    }

    @Override
    public DisposableManager getManager() {
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

    public P getNonPresenter() {
        return getPresenter();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.p != null) {
            this.p.dropView();
        }
    }
}
