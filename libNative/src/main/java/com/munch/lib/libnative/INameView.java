package com.munch.lib.libnative;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Munch on 2018/12/25 23:53.
 */
public interface INameView<T> extends IRootView {

    void loadData(@NonNull T t);

    void loadEmpty();

    void loadFail();

    @Override
    default void syncViewByType(@Type int type, @Nullable Object... objs) {
        switch (type) {
            case Type.TYPE_DATA_SUCCESS:
                loadData(ConvertHelper.convert(objs));
                break;
            case Type.TYPE_DATA_EMPTY:
                loadEmpty();
                break;
            case Type.TYPE_DATA_FAIL:
                loadFail();
                break;
            case Type.TYPE_UN_GET_ERROR:
                break;
            default:
                syncView(type, objs);
        }
    }

    void syncView(int type, Object... objs);

}
