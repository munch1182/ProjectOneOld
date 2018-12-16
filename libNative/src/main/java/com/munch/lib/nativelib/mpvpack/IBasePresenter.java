package com.munch.lib.nativelib.mpvpack;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.munch.lib.nativelib.exception.MethodCallException;

/**
 * 此接口内只进行逻辑操作，不涉及数据的实际保存或缓存，不涉及UI变化如Dialog、Toast
 * <p>
 * Created by Munch on 2018/12/15.
 */
public interface IBasePresenter<V extends IBaseView, M extends IModel> {

    /**
     * 绑定View
     */
    IBasePresenter<V,M> takeView(@Nullable V v);

    /**
     * 销毁View
     */
    void dropView();

    /**
     * 获取View，可能为空
     */
    @Nullable
    default V getView() {
        return null;
    }

    /**
     * 在保证View不为空的时候获取View，否则会抛出异常
     */
    @NonNull
    V getNonNullView() throws MethodCallException;

    /**
     * 设置Model
     */
    @NonNull
    M putModel(@NonNull M m);

    /**
     * 获取Model
     */
    @NonNull
    M getModel();

    /**
     * presenter的入口方法，带有类型区分和参数
     */
    default void start(int type, @Nullable Object... parameters) {
    }

    /**
     * View操作引起的数据变化
     */
    default void modifyData(int type, @Nullable Object... parameters) {
    }

}
