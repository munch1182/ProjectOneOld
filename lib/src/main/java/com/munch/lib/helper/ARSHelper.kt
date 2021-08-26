@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * 专门用于设置add、remove、set三个方法的工具类
 * 但其实主要是提取set方法
 *
 * LifecycleOwner的相关方法晚于activity的相关方法执行
 *
 * Create by munch1182 on 2020/12/28 13:51.
 */
interface ARSHelper<T> {

    /**
     * 因为没有名字，所以不要直接使用，实现类应该建立有名字的方法返回本值，然后用方法调用
     */
    val arrays: MutableList<T>

    fun add(t: T): ARSHelper<T> {
        if (!arrays.contains(t)) {
            arrays.add(t)
        }
        return this
    }

    fun remove(t: T): ARSHelper<T> {
        if (arrays.contains(t)) {
            arrays.remove(t)
        }
        return this
    }

    fun setOnState(
        owner: LifecycleOwner, t: T, onDestroy: (() -> Unit)? = null
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

    fun setOnState(owner: LifecycleOwner, t: T): ARSHelper<T> = setOnState(owner, t, null)

    /**
     * 未考虑线程的问题
     */
    fun notifyListener(notify: (T) -> Unit) {
        arrays.forEach { notify.invoke(it) }
    }

    fun setWhenCreate(
        owner: LifecycleOwner, t: T, onCreate: (() -> Unit)? = null, onDestroy: (() -> Unit)? = null
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

    fun setWhenStart(
        owner: LifecycleOwner, t: T, onStart: (() -> Unit)? = null, onStop: (() -> Unit)? = null
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

    fun setWhenResume(
        owner: LifecycleOwner, t: T, onResume: (() -> Unit)? = null, onPause: (() -> Unit)? = null
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

    fun clear(): ARSHelper<T> {
        arrays.clear()
        return this
    }
}

class SimpleARSHelper<T> : ARSHelper<T> {
    private val list = mutableListOf<T>()
    override val arrays: MutableList<T>
        get() = list
}