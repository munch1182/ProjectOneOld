package com.munch.common.base.mvp

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent

/**
 * Created by Munch on 2018/12/8.
 */
interface IPresenter<V : IView> {

    var v: V?

    fun takeView(v: V): IPresenter<V> {
        this.v = v
        return this
    }

    fun manageView(): IPresenter<V> {
        if (v !is LifecycleOwner) {
            throw UnsupportedOperationException()
        }
        (v as LifecycleOwner).lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dropView()
            }
        })
        return this
    }

    fun dropView() {
        v = null
    }

    fun start()
}