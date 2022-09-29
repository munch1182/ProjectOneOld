package com.munch.lib.android.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.munch.lib.android.AppHelper

/**
 * 用于简化[BroadcastReceiver]的实现
 *
 * Create by munch1182 on 2022/9/29 9:13.
 */
abstract class ReceiverHelper<T>(private val actions: Array<String>) : ARSHelper<T>() {

    protected val content: Context = AppHelper
    protected open var receiver: BroadcastReceiver? = null

    fun register() {
        if (receiver != null) throw IllegalStateException("")
        receiver = Receiver()
        content.registerReceiver(receiver!!, createIntentFilter())
    }

    fun unregister() {
        receiver ?: return
        content.unregisterReceiver(receiver)
        clear()
        receiver = null
    }

    protected open fun createIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            actions.forEach { addAction(it) }
        }
    }

    protected inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            actions.find { it == action }
                ?.let { dispatchAction(this@ReceiverHelper.content, action, intent) }
        }
    }

    /**
     * 收到了某个通知, 需要通知[ARSHelper]
     */
    protected abstract fun dispatchAction(context: Context, action: String, intent: Intent)
}