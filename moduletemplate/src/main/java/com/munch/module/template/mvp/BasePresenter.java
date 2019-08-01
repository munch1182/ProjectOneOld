package com.munch.module.template.mvp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.munch.lib.libnative.excetion.MethodException;
import com.munch.module.template.net.NetManager;
import com.munch.module.template.net.Service;
import com.munch.module.template.rxjava.DisposableManager;
import com.munch.module.template.rxjava.IDisposableManager;

/**
 * Created by Munch on 2019/7/29 17:28
 */
public class BasePresenter<V extends IView> implements IPresenter<V> {

    private V v;


    @Override
    public V attachView(V v) {
        this.v = v;
        return v;
    }

    public IDisposableManager getManager() {
        if (null == getView()) {
            return DisposableManager.newInstance();
        }
        return getView().getManager();
    }

    @Override
    public void detachView() {
        v = null;
    }

    @Nullable
    @Override
    public V getView() {
        return v;
    }

    /**
     * @return 当判定或者其它方法确认View不为null时，调用此方法，否则，应该调用{@link #getView()}
     * @see com.munch.module.template.rxjava.ResDtoObserver
     */
    @NonNull
    public V getNonView() {
        V view = getView();
        if (view != null) {
            return view;
        }
        throw MethodException.unSupport();
    }

    @Override
    public boolean isViewAttach() {
        return v != null;
    }

    public Service getService() {
        return NetManager.getInstance().getService();
    }
}
