package com.munch.lib.android.helper

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.function.Update
import java.util.*

/**
 * 通用的添加、移除操作
 */
interface IARSHelper<T> {
    /**
     * 添加
     */
    fun add(t: T)

    /**
     * 移除
     */
    fun remove(t: T)

    /**
     * 使用[LifecycleOwner]自动添加移除
     */
    fun set(owner: LifecycleOwner, t: T)

    /**
     * 清除所有
     */
    fun clear()
}

open class ARSHelper<T> : IARSHelper<T> {

    protected open val list = mutableListOf<T>()

    override fun add(t: T) {
        list.add(t)
    }

    override fun remove(t: T) {
        list.remove(t)
    }

    override fun set(owner: LifecycleOwner, t: T) {
        add(t)
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                remove(t)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    override fun clear() {
        list.clear()
    }

    open fun update(update: Update<T>) {
        list.forEach { update.invoke(it) }
    }
}

fun <T> ARSHelper<T>.setOnResume(owner: LifecycleOwner, t: T) {
    owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            add(t)
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            remove(t)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            owner.lifecycle.removeObserver(this)
        }
    })
}

class ARSConcurrentHelper<T> : ARSHelper<T>() {
    override val list: MutableList<T> = Collections.synchronizedList(super.list)
}