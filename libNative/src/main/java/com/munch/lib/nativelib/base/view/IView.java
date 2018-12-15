package com.munch.lib.nativelib.base.view;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * Created by Munch on 2018/12/15.
 */
public interface IView<T> {

    @Nullable
    Context getViewContext();

    /**
     * 用于请求数据后的视图展示
     */
    void loadData(@Nullable T t);

    /**
     * 用于请求数据失败后的显示
     * @see #syncView(int, Object...)
     */
    void loadDataFail(@Nullable Object...parameters);

    /**
     * 用于请求操作后部分数据更新的视图同步
     */
    void syncView(int type, @Nullable Object...parameters);

}
