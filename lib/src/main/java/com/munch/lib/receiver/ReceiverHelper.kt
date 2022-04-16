package com.munch.lib.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.AppHelper
import com.munch.lib.helper.ARSHelper

/**
 * Create by munch1182 on 2022/4/16 14:01.
 */
abstract class ReceiverHelper<T> constructor(
    private val context: Context = AppHelper.app,
    private val actions: Array<String>
) : ARSHelper<T>() {

    private var delegate: ReceiverDelegate? = null

    fun register() {
        if (delegate != null) {
            return
        }
        delegate = ReceiverDelegate()
        context.registerReceiver(delegate, IntentFilter().apply {
            actions.forEach { addAction(it) }
            buildIntentFilter(this)
        })
    }

    fun unregister() {
        delegate ?: return
        context.unregisterReceiver(delegate)
        list.clear()
        delegate = null
    }

    override fun set(owner: LifecycleOwner, t: T) {
        super.set(owner, t)
        register()
        unregisterWhenDestroy(owner)
    }

    override fun setWhenResume(owner: LifecycleOwner, t: T) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                add(t)
                register()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                remove(t)
                unregister()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    override fun setWhenStart(owner: LifecycleOwner, t: T) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onResume(owner)
                add(t)
                register()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onPause(owner)
                remove(t)
                unregister()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    private fun unregisterWhenDestroy(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
                unregister()
            }
        })
    }

    inner class ReceiverDelegate : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            actions.find { it == intent.action }?.let {
                notifyUpdate { handleAction(this@ReceiverHelper.context, intent.action!!, intent) }
            }
            afterNotify()
        }

    }

    protected open fun afterNotify() {
    }

    protected open fun buildIntentFilter(filter: IntentFilter) {
    }

    abstract fun handleAction(context: Context, action: String, intent: Intent)
}