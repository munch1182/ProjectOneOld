@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.pre.lib.helper

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * 专门用于设置add、remove、set三个方法的工具类
 * 但其实主要是提取set方法
 *
 * Create by munch1182 on 2020/12/28 13:51.
 */
abstract class ARSHelper<T> {

    /**
     * 因为没有名字，所以不要直接使用，实现类应该建立有名字的方法返回本值，然后用方法调用
     */
    protected val arrays: ArrayList<T> = arrayListOf()

    open fun add(t: T): ARSHelper<T> {
        if (!arrays.contains(t)) {
            arrays.add(t)
        }
        return this
    }

    open fun remove(t: T): ARSHelper<T> {
        if (arrays.contains(t)) {
            arrays.remove(t)
        }
        return this
    }

    open fun set(
        owner: LifecycleOwner,
        t: T,
        onDestroy: (() -> Unit)? = null
    ): ARSHelper<T> {
        add(t)
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                onDestroy?.invoke()
                remove(t)
                owner.lifecycle.removeObserver(this)
            }
        })
        return this
    }

    /**
     * 未考虑线程的问题
     */
    open fun notifyListener(notify: (T) -> Unit) {
        arrays.forEach { notify.invoke(it) }
    }

    @JvmOverloads
    open fun setWhenCreate(
        owner: LifecycleOwner, t: T, onCreate: (() -> Unit)? = null,
        onDestroy: (() -> Unit)? = null
    ): ARSHelper<T> {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                add(t)
                onCreate?.invoke()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                remove(t)
                onDestroy?.invoke()
                owner.lifecycle.removeObserver(this)
            }
        })
        return this
    }

    @JvmOverloads
    open fun setWhenStart(
        owner: LifecycleOwner, t: T, onStart: (() -> Unit)? = null,
        onStop: (() -> Unit)? = null
    ): ARSHelper<T> {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                add(t)
                onStart?.invoke()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                remove(t)
                onStop?.invoke()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                owner.lifecycle.removeObserver(this)
            }
        })
        return this
    }

    @JvmOverloads
    open fun setWhenResume(
        owner: LifecycleOwner, t: T, onResume: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null
    ): ARSHelper<T> {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                add(t)
                onResume?.invoke()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                remove(t)
                onPause?.invoke()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                owner.lifecycle.removeObserver(this)
            }
        })
        return this
    }

    open fun clear(): ARSHelper<T> {
        arrays.clear()
        return this
    }
}