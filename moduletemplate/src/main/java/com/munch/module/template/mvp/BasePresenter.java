package com.munch.module.template.mvp;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;
import com.munch.module.template.net.NetManager;
import com.munch.module.template.net.Service;
import com.munch.module.template.rxjava.DisposableManager;
import com.munch.module.template.rxjava.RxTransHelper;

/**
 * Created by Munch on 2019/7/29 17:28
 */
public class BasePresenter<V extends IView> implements IPresenter<V> {

    private V v;


    @Override
    public V takeView(V v) {
        this.v = v;
        return v;
    }

    public DisposableManager getManager() {
        if (null == getView()) {
            return DisposableManager.newInstance();
        }
        return getView().getManager();
    }

    @Override
    public void dropView() {
        v = null;
    }

    @Nullable
    @Override
    public V getView() {
        return v;
    }

    @Override
    public boolean hasView() {
        return v == null;
    }

    public Service getService() {
        return NetManager.getInstance().getService();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void aVoid() {
        getService().login("", "")
                .compose(RxTransHelper.dto(this, getManager()))
                .subscribe(res -> {
                });
    }
}
