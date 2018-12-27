package com.munch.lib.libnative;

/**
 * Created by Munch on 2018/12/27 15:42.
 */
public interface INameView<T> extends IRootView {

    void onLoadDataSuccess(T t);

    void onLoadDataFail(String reason);

    void onLoadEmpty();

}
