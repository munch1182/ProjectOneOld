@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

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

    fun set(owner: LifecycleOwner, t: T, onDestroy: (() -> Unit)? = null): ARSHelper<T> {
        add(t)
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                onDestroy?.invoke()
                remove(t)
                owner.lifecycle.removeObserver(this)
            }
        })
        return this
    }

    fun set(owner: LifecycleOwner, t: T): ARSHelper<T> = set(owner, t, null)

    /**
     * 未考虑线程的问题
     */
    fun notifyListener(notify: (T) -> Unit) {
        arrays.forEach { notify.invoke(it) }
    }

    fun setWhenCreate(
        owner: LifecycleOwner, t: T, onCreate: (() -> Unit)? = null, onDestroy: (() -> Unit)? = null
    ): ARSHelper<T> {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                add(t)
                onCreate?.invoke()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
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
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                add(t)
                onStart?.invoke()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                remove(t)
                onStop?.invoke()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
            }
        })
        return this
    }

    fun setWhenResume(
        owner: LifecycleOwner, t: T, onResume: (() -> Unit)? = null, onPause: (() -> Unit)? = null
    ): ARSHelper<T> {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                add(t)
                onResume?.invoke()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                remove(t)
                onPause?.invoke()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
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