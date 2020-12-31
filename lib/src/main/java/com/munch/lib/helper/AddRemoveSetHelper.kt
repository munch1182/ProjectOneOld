package com.munch.lib.helper

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * 专门用于设置三个方法的工具类
 * Create by munch1182 on 2020/12/28 13:51.
 */
abstract class AddRemoveSetHelper<T> {

    val arrays: ArrayList<T> = arrayListOf()

    open fun add(t: T) {
        if (!arrays.contains(t)) {
            arrays.add(t)
        }
    }

    open fun remove(t: T) {
        if (arrays.contains(t)) {
            arrays.remove(t)
        }
    }

    open fun set(owner: LifecycleOwner, t: T, onDestroy: (() -> Unit)? = null) {
        add(t)
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                onDestroy?.invoke()
                remove(t)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    open fun setWhenCreate(
        owner: LifecycleOwner, t: T, onCreate: (() -> Unit)? = null,
        onDestroy: (() -> Unit)? = null
    ) {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                onCreate?.invoke()
                add(t)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                onDestroy?.invoke()
                remove(t)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    open fun setWhenResume(
        owner: LifecycleOwner, t: T, onResume: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null
    ) {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                add(t)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                remove(t)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    open fun clear(): AddRemoveSetHelper<T> {
        arrays.clear()
        return this
    }
}