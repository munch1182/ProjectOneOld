package com.munch.lib.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * 广播动态注册的通用部分类
 * 使用需继承并实现[handleAction]
 *
 * Create by munch1182 on 2020/12/28 16:48.
 */
abstract class ReceiverHelper<T> constructor(
    private val context: Context,
    private val actions: Array<String>
) : AddRemoveSetHelper<T>() {

    private var receiver: ScreenReceiver? = null

    open fun register() {
        receiver = ScreenReceiver()
        context.registerReceiver(receiver, IntentFilter().apply {
            actions.forEach { addAction(it) }
        })
    }

    fun setAndRegister(owner: LifecycleOwner, t: T, onDestroy: (() -> Unit)?) {
        register()
        super.set(owner, t, onDestroy)
        unregister(owner)
    }

    fun setAndRegisterWhenCreate(
        owner: LifecycleOwner,
        t: T,
        onCreate: (() -> Unit)?,
        onDestroy: (() -> Unit)?
    ) {
        registerWhenCreate(owner)
        super.setWhenCreate(owner, t, onCreate, onDestroy)
    }

    open fun registerWhenCreate(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                register()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                unregister()
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    private fun unregister(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                unregister()
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    open fun unregister() {
        receiver ?: return
        context.unregisterReceiver(receiver)
        receiver = null
    }

    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if (arrays.isEmpty()) {
                return
            }
            actions.forEach { action ->
                if (action == intent.action) {
                    arrays.forEach {
                        handleAction(action, context, intent, it)
                    }
                    return
                }
            }
        }
    }

    abstract fun handleAction(action: String, context: Context?, intent: Intent, t: T)
}