package com.munch.lib.nativelib.mpvpack;

import android.support.annotation.Nullable;

/**
 * 数据操作的回调
 * <p>
 * Created by Munch on 2018/12/15.
 */
public interface OnDataListener<T> {

    /**
     * 数据操作成功
     * 参数是否为可能空需要调用时机确定
     */
    default void onDataSuccess(T t) {
    }

    /**
     * 数据操作失败
     */
    default void onDataFail(int code, @Nullable Object... parameters) {
    }
}
