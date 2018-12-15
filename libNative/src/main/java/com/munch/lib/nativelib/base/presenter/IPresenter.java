package com.munch.lib.nativelib.base.presenter;

import android.support.annotation.Nullable;

/**
 * Created by Munch on 2018/12/15.
 */
public interface IPresenter {

    /**
     * presenter的入口
     */
    void start();

    void start(int type);

    void start(int type, @Nullable Object... parameters);

    void modifyData(int type, @Nullable Object... parameters);


}
