@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.helper.ARSHelper

/**
 * 广播动态注册的通用部分类
 * 使用需继承并实现[handleAction]并在需要的地方[register]和[unregisterWhenDestroy]或者使用[setAndRegister]
 *
 * Create by munch1182 on 2020/12/28 16:48.
 */
abstract class ReceiverHelper<T> constructor(
    private val context: Context,
    private val actions: Array<String>
) : ARSHelper<T> {

    private var receiver: Receiver? = null
    private val list = mutableListOf<T>()
    override val arrays: MutableList<T>
        get() = list

    open fun register() {
        receiver = Receiver()
        context.registerReceiver(receiver, IntentFilter().apply {
            actions.forEach { addAction(it) }
            buildIntentFilter(this)
        })
    }

    fun setAndRegister(owner: LifecycleOwner, t: T, onDestroy: (() -> Unit)? = null) {
        register()
        super.set(owner, t, onDestroy)
        unregisterWhenDestroy(owner)
    }

    fun setAndRegisterWhenCreate(
        owner: LifecycleOwner,
        t: T,
        onCreate: (() -> Unit)? = null,
        onDestroy: (() -> Unit)? = null
    ) {
        registerWhenCreate(owner)
        super.setWhenCreate(owner, t, onCreate, onDestroy)
    }

    open fun registerWhenCreate(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                register()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                unregister()
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    private fun unregisterWhenDestroy(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                unregister()
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    open fun unregister() {
        receiver ?: return
        context.unregisterReceiver(receiver)
        getReceiverArrays().clear()
        receiver = null
    }

    private fun getReceiverArrays() = arrays

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if (getReceiverArrays().isEmpty()) {
                return
            }
            actions.forEach { action ->
                if (action == intent.action) {
                    getReceiverArrays().forEach { handleAction(action, context, intent, it) }
                    return
                }
            }
            afterActionNotify(intent.action, context, intent)
        }
    }

    open fun buildIntentFilter(intent: IntentFilter) {}

    open fun afterActionNotify(action: String?, context: Context?, intent: Intent) {}

    abstract fun handleAction(action: String, context: Context?, intent: Intent, t: T)
}