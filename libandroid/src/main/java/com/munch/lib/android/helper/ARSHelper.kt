package com.munch.lib.android.helper

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.define.Notify
import com.munch.lib.android.define.Update
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
     * 使用[LifecycleOwner]自动添加移除, 其时机由[ILifecycle]触发
     */
    fun set(owner: ILifecycle, t: T)

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

    override fun set(owner: ILifecycle, t: T) {
        add(t)
        owner.onInactive { remove(t) }
    }

    override fun clear() {
        list.clear()
    }

    protected open fun update(update: Update<T>) {
        list.forEach { update.invoke(it) }
    }
}

class ARSParameterHelper<T> : ARSHelper<T>() {
    fun update2(update: Update<T>) {
        update(update)
    }
}

fun <T> ARSHelper<T>.set(owner: LifecycleOwner, t: T) = set(owner.toILifecycle(), t)
fun <T> ARSHelper<T>.setWhenResume(owner: LifecycleOwner, t: T) =
    set(owner.toILifecycleByResume(), t)

class ARSConcurrentHelper<T> : ARSHelper<T>() {
    override val list: MutableList<T> = Collections.synchronizedList(super.list)
}

/**
 * 用以单纯描述 开始/结束 两个时机
 */
interface ILifecycle {

    fun onActive(onCreate: Notify)
    fun onInactive(onDestroy: Notify)
}

class MutableLifecycle : ILifecycle {

    private var onCreate: MutableList<Notify>? = null
    private var onDestroy: MutableList<Notify>? = null
    private var isActive = false

    override fun onActive(onCreate: Notify) {
        if (this.onCreate == null) {
            this.onCreate = mutableListOf()
        }
        this.onCreate?.add(onCreate)
        if (isActive) {
            onCreate.invoke()
        }
    }

    override fun onInactive(onDestroy: Notify) {
        if (this.onDestroy == null) {
            this.onDestroy = mutableListOf()
        }
        this.onDestroy?.add(onDestroy)
        if (!isActive) {
            onDestroy.invoke()
        }
    }

    fun active() {
        isActive = true
        this.onCreate?.forEach { it.invoke() }
    }

    fun inactive() {
        isActive = false
        this.onDestroy?.forEach { it.invoke() }
    }
}

//<editor-fold desc="fromLifecycleOwner">
fun LifecycleOwner.toILifecycle(): ILifecycle {
    val life = MutableLifecycle()
    this.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            life.active()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            life.inactive()
            owner.lifecycle.removeObserver(this)
        }
    })
    return life
}

fun LifecycleOwner.toILifecycleByResume(): ILifecycle {
    val life = MutableLifecycle()
    this.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            life.active()
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            life.inactive()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            owner.lifecycle.removeObserver(this)
        }
    })
    return life
}
//</editor-fold>