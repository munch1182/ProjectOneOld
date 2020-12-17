package com.munch.project.testsimple.alive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.munch.lib.log

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

    fun register() {
        if (receiver != null) {
            return
        }
        log("ScreenReceiver register")
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
        log("ScreenReceiver unregister")
    }

    interface ScreenStateListener {
        fun onScreenOn(context: Context?)
        fun onScreenOff(context: Context?)
        fun onUserPresent(context: Context?)
    }

    inner class ScreenReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            log("ScreenReceiver onReceive: ${intent?.action},${list.isEmpty()}")
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