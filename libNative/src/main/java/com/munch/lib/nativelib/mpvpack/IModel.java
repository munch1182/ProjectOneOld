package com.munch.lib.nativelib.mpvpack;

import android.support.annotation.Nullable;

/**
 * 此接口内只进行数据保存，不涉及逻辑操作如判断等，不涉及UI变化如Dialog、Toast
 * 如果设计多个类型数据，建议封装数据或者使用多个IModel
 * <p>
 * Created by Munch on 2018/12/14.
 */
public interface IModel<T> {

    /**
     * 获取数据类
     */
    @Nullable
    default T getData(@Nullable OnDataListener<T> l, @Nullable Object... parameters) {
        return null;
    }

    /**
     * 数据存储，带有回调
     */
    default void saveData(@Nullable T t, @Nullable OnDataListener<T> l) {
    }

}
