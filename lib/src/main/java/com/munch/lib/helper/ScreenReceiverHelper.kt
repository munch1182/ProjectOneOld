package com.munch.lib.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Create by munch1182 on 2020/12/14 11:08.
 */
class ScreenReceiverHelper constructor(private val context: Context) :
    AddRemoveSetHelper<ScreenReceiverHelper.ScreenStateListener>() {

    private var receiver: ScreenReceiver? = null

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
        arrays.clear()
    }

    interface ScreenStateListener {
        fun onScreenOn(context: Context?)
        fun onScreenOff(context: Context?)
        fun onUserPresent(context: Context?)
    }

    inner class ScreenReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if (arrays.isEmpty()) {
                return
            }
            when {
                Intent.ACTION_SCREEN_ON == intent.action -> {
                    arrays.forEach {
                        it.onScreenOn(context)
                    }
                }
                Intent.ACTION_SCREEN_OFF == intent.action -> {
                    arrays.forEach {
                        it.onScreenOff(context)
                    }
                }
                //解锁
                Intent.ACTION_USER_PRESENT == intent.action -> {
                    arrays.forEach {
                        it.onUserPresent(context)
                    }
                }
            }
        }
    }
}