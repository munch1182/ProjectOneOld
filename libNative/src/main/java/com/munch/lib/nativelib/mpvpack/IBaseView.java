package com.munch.lib.nativelib.mpvpack;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * 此接口内只进行UI更新如Dialog、Toast等，除对方法的参数判断外不涉及其它逻辑操作，不涉及数据保存、缓存
 * <p>
 * Created by Munch on 2018/12/15.
 */
public interface IBaseView {

    @Nullable
    default Context getViewContext() {
        return null;
    }

    /**
     * 用于请求操作后部分数据更新的视图同步
     */
    default void syncAllView(int type, @Nullable Object... parameters) {
    }

}
