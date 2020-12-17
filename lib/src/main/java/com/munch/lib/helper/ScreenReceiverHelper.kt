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
 * Create by munch1182 on 2020/12/14 11:08.
 */
class ScreenReceiverHelper constructor(private val context: Context) {

    private var list: ArrayList<ScreenStateListener> = arrayListOf()
    private var receiver: ScreenReceiver? = null

    fun addScreenStateListener(listener: ScreenStateListener): ScreenReceiverHelper {
        if (!list.contains(listener)) {
            list.add(listener)
        }
        return this
    }

    fun addScreenStateListener(
        owner: LifecycleOwner,
        listener: ScreenStateListener
    ): ScreenReceiverHelper {
        addScreenStateListener(listener)
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun destroy() {
                removeScreenStateListener(listener)
                owner.lifecycle.removeObserver(this)
            }
        })
        return this
    }

    fun removeScreenStateListener(listener: ScreenStateListener): ScreenReceiverHelper {
        if (list.contains(listener)) {
            list.remove(listener)
        }
        return this
    }

    fun register() {
        if (receiver != null) {
            return
        }
        receiver = ScreenReceiver()
        context.registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        })
    }

    fun unregister() {
        context.unregisterReceiver(receiver)
        receiver = null
        list.clear()
    }

    interface ScreenStateListener {
        fun onScreenOn(context: Context?)
        fun onScreenOff(context: Context?)
        fun onUserPresent(context: Context?)
    }

    inner class ScreenReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if (list.isEmpty()) {
                return
            }
            when {
                Intent.ACTION_SCREEN_ON == intent.action -> {
                    list.forEach {
                        it.onScreenOn(context)
                    }
                }
                Intent.ACTION_SCREEN_OFF == intent.action -> {
                    list.forEach {
                        it.onScreenOff(context)
                    }
                }
                //解锁
                Intent.ACTION_USER_PRESENT == intent.action -> {
                    list.forEach {
                        it.onUserPresent(context)
                    }
                }
            }
        }
    }
}