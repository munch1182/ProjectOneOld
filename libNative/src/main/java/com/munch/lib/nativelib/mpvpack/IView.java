package com.munch.lib.nativelib.mpvpack;

import android.support.annotation.Nullable;

import com.munch.lib.nativelib.base.Type;
import com.munch.lib.nativelib.exception.MethodCallException;
import com.munch.lib.nativelib.constant.C;
import com.munch.lib.nativelib.helper.ObjectHelper;

/**
 * Created by Munch on 2018/12/16.
 */
public interface IView<T> extends IBaseView {

    default void loadData(int type, T data) {
    }

    default void loadDataFail(int type, @Nullable Object... parameters) {
    }

    default void notice(int type, @Nullable Object... parameters) {
    }

    @Override
    default void syncAllView(int type, @Nullable Object... parameters) {
        switch (type) {
            case Type.TYPE_LOAD_SUCCESS:
                loadData(type, parameterConvert2Bean(parameters));
                break;
            case Type.TYPE_LOAD_FAIL:
                loadDataFail(type, parameters);
                break;
            case Type.TYPE_NOTICE:
                notice(type, parameters);
                break;
            case Type.TYPE_REFRESH:
                refresh(type, parameters);
                break;
            default:
                syncView(type, parameters);
                break;
        }
    }

    default void refresh(int type, @Nullable Object... parameters) {
    }

    default void syncView(int type, @Nullable Object... parameters) {
    }

    @Nullable
    @SuppressWarnings("unchecked")
    default T parameterConvert2Bean(@Nullable Object... parameters) throws MethodCallException {
        if (ObjectHelper.hasEmpty(parameters)) {
            throw new MethodCallException(C.Exception.ERROR_NULL_POINT);
        }
        return (T) ObjectHelper.requireNonNull(parameters)[0];
    }

    /**
     * 根据类型将参数转为实际的类型
     */
    @Nullable
    default <K> K parameterConvert(int type, @Nullable Object... parameters) {
        return null;
    }
}
