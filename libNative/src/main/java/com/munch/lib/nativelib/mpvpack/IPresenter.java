package com.munch.lib.nativelib.mpvpack;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

import com.munch.lib.nativelib.base.Type;

/**
 * Created by Munch on 2018/12/16.
 */
public interface IPresenter<T,V extends IView<T>, M extends IModel> extends IBasePresenter<V, M> {

    /**
     * 添加view并自动销毁view
     */
    default IBasePresenter<V, M> manageView(@NonNull V v, @NonNull LifecycleOwner l) {
        l.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy() {
                dropView();
            }
        });
        return takeView(v);
    }

    /**
     * 入口方法
     */
    default void start() {
        start(Type.TYPE_DEFAULT);
    }

    /**
     * 入口方法
     */
    default void start(int type) {
        start(type, (Object) null);
    }


}
