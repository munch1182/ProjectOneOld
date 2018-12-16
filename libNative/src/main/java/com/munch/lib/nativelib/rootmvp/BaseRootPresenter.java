package com.munch.lib.nativelib.rootmvp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.nativelib.exception.MethodCallException;
import com.munch.lib.nativelib.mpvpack.IModel;
import com.munch.lib.nativelib.mpvpack.IBasePresenter;
import com.munch.lib.nativelib.mpvpack.IPresenter;
import com.munch.lib.nativelib.mpvpack.IBaseView;
import com.munch.lib.nativelib.mpvpack.IView;
import com.munch.lib.nativelib.mpvpack.OnDataListener;
import com.munch.lib.nativelib.constant.C;
import com.munch.lib.nativelib.helper.ObjectHelper;

import java.lang.ref.WeakReference;

/**
 * Created by Munch on 2018/12/14.
 */
public abstract class BaseRootPresenter<T, V extends IView<T>, M extends IModel<T>>
        implements IPresenter<T,V, M> {

    private WeakReference<V> mRefView;
    private M m;

    /**
     * @throws RuntimeException 绑定之前调用了此方法
     * @see BaseRootPresenter#takeView(IBaseView)
     * @see IPresenter#manageView(com.munch.lib.nativelib.mpvpack.IView, android.arch.lifecycle.LifecycleOwner)
     */
    @Nullable
    public V getView() throws MethodCallException {
        if (ObjectHelper.isEmpty(mRefView)) {
            throw new RuntimeException(C.Exception.ERROR_DO_WRONG_TIME);
        }
        return mRefView.get();
    }


    @Override
    public void start(int type, @Nullable Object... parameters) {
        m.getData(new OnDataListener<T>() {
            @Override
            public void onDataSuccess(T t) {
                getNonNullView().loadData(type, t);
            }

            @Override
            public void onDataFail(int code, @Nullable Object... parameters) {
                getNonNullView().loadDataFail(type, parameters);
            }
        }, parameters);
    }

    @Override
    public void modifyData(int type, @Nullable Object... parameters) {
        getNonNullView().syncAllView(type, parameters);
    }


    @NonNull
    public M getModel() {
        return this.m;
    }

    @NonNull
    public M putModel(@NonNull M m) {
        this.m = m;
        return this.m;
    }

    @NonNull
    public V getNonNullView() throws MethodCallException {
        if (ObjectHelper.hasEmpty(mRefView, mRefView.get())) {
            throw new RuntimeException(C.Exception.ERROR_DO_WRONG_TIME);
        }
        return mRefView.get();
    }


    @Override
    public IBasePresenter<V, M> takeView(@Nullable V v) {
        mRefView = new WeakReference<>(v);
        return this;
    }

    public void dropView() {
        if (ObjectHelper.isNonEmpty(mRefView)) {
            mRefView.clear();
        }
    }
}
