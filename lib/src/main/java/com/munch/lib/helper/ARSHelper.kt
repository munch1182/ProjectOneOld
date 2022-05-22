package com.munch.lib.helper

import android.os.Handler
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.UnImplException

/**
 *
 * 此类简单用于ADD/REMOVE的场景
 *
 * 1. 需要注意并发的问题
 * 2. LifecycleObserver的回调时机是比生命周期要晚的，需要注意回调顺序的问题
 * 3. 如果是在诸如广播中使用，需要注意notifyUpdate不要阻塞主线程
 *
 * Created by munch1182 on 2022/4/9 3:55.
 */
interface IARSHelper<T> {

    fun add(t: T)

    fun remove(t: T)

    fun notifyUpdate(update: (T) -> Unit)

    fun clear()

    /**
     * 在调用的时候添加，在onDestroy的时候移除
     */
    fun set(owner: LifecycleOwner, t: T) {
        add(t)
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
                remove(t)
            }
        })
    }

    /**
     * 在onResume的时候添加，在onPause的时候移除
     */
    fun setWhenResume(owner: LifecycleOwner, t: T) {
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

    /**
     * 在onStart的时候添加，在onStop的时候移除
     */
    fun setWhenStart(owner: LifecycleOwner, t: T) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onResume(owner)
                add(t)
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onPause(owner)
                remove(t)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    val count: Int
}

interface ISet<T> {

    /**
     * 一次性设置，此方法需要自行实现
     */
    fun set(t: T) {
        throw UnImplException()
    }
}

open class ARSHelper<T>(protected open var _handler: Handler? = null) : IARSHelper<T> {

    protected open val list = mutableListOf<T>()

    open fun setHandler(handler: Handler?) {
        this._handler = handler
    }

    override fun add(t: T) {
        synchronized(this) {
            if (!list.contains(t)) {
                list.add(t)
            }
        }
    }

    override fun remove(t: T) {
        synchronized(this) {
            if (list.contains(t)) {
                list.remove(t)
            }
        }
    }

    override fun notifyUpdate(update: (T) -> Unit) {
        synchronized(this) {
            list.forEach { _handler?.post { update.invoke(it) } ?: update.invoke(it) }
        }
    }

    override fun clear() {
        list.clear()
    }

    override val count: Int
        get() = list.size

}

open class ARSSHelper<T>(handler: Handler? = null) : ARSHelper<T>(handler), ISet<T> {

    protected open val onceList = mutableListOf<T>()

    override fun set(t: T) {
        synchronized(this) {
            if (!onceList.contains(t)) {
                onceList.add(t)
            }
        }
    }

    override fun notifyUpdate(update: (T) -> Unit) {
        super.notifyUpdate(update)
        synchronized(this) {
            onceList.forEach { _handler?.post { update.invoke(it) } ?: update.invoke(it) }
            onceList.clear()
        }
    }

    override val count: Int
        get() = list.size
}