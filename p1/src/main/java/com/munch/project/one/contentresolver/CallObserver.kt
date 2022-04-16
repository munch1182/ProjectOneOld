@file:Suppress("DEPRECATION")

package com.munch.project.one.contentresolver

import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.munch.lib.AppHelper
import com.munch.lib.base.OnReceive
import com.munch.lib.fast.notification.NotificationHelper
import com.munch.lib.log.InfoStyle
import com.munch.lib.log.Logger
import com.munch.lib.receiver.ReceiverHelper
import com.munch.lib.task.ThreadPoolHelper

/**
 * Create by munch1182 on 2022/4/16 13:49.
 */
class CallObserver(
    private val context: Context = AppHelper.app,
    private val log: Logger = Logger(
        "CallObserver",
        infoStyle = InfoStyle.THREAD_ONLY
    )
) {

    private var phoneHanding = false
    private val phoneState = PhoneStateReceiver()
    private val phoneStateListener = object : PhoneStateListener() {
        @Deprecated(
            "Deprecated in Java", ReplaceWith(
                "super.onCallStateChanged(state, phoneNumber)",
                "android.telephony.PhoneStateListener"
            )
        )
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            log.log("onCallStateChanged: $state, $phoneNumber")
            NotificationHelper.getInstance()
                .notify("onCallStateChanged: $state, $phoneNumber", 1101)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    phoneHanding = true
                    onIncoming(phoneNumber)
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (phoneHanding) {
                        phoneHanding = false
                        onRefuse(phoneNumber)
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    if (phoneHanding) {
                        phoneHanding = false
                        onAnswer(phoneNumber)
                    }
                }
            }
        }
    }

    /*@RequiresApi(Build.VERSION_CODES.S)
    private val teleCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            log.log("onCallStateChanged: $state")
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    phoneHanding = true
                    onIncoming(null)
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (phoneHanding) {
                        phoneHanding = false
                        onRefuse(null)
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    if (phoneHanding) {
                        phoneHanding = false
                        onAnswer(null)
                    }
                }
            }
        }

    }*/
    private val tm: TelephonyManager?
        get() = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    private val pool by lazy { ThreadPoolHelper.newCachePool() }

    private fun onAnswer(phoneNumber: String?) {
        log.log("onAnswer: $phoneNumber")
    }

    private fun onRefuse(phoneNumber: String?) {
        log.log("onRefuse: $phoneNumber")
    }

    private fun onIncoming(phoneNumber: String?) {
        log.log("onIncoming: $phoneNumber")
    }

    fun listener() {
        tm?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        log.log("listener phoneState")
    }

    fun noListener() {
        tm?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        log.log("no listener phoneState")
    }

    fun listenerOnS() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm?.registerTelephonyCallback(pool, teleCallback)
            log.log("listener phoneState on S")
        } else {
            log.log("${Build.VERSION.SDK_INT} < Build.VERSION_CODES.S")
        }*/
    }

    fun unListenerOnS() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm?.unregisterTelephonyCallback(teleCallback)
            log.log("no listener phoneState on S")
        }*/
    }

    fun register() {
        phoneState.register()
        log.log("register receiver")
    }

    fun unregister() {
        phoneState.unregister()
        log.log("unregister receiver")
    }

    inner class PhoneStateReceiver : ReceiverHelper<OnReceive<String>>(
        context,
        arrayOf("android.intent.action.PHONE_STATE")
    ) {
        override fun handleAction(context: Context, action: String, intent: Intent) {
            log.log("$action, ${intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)}")
            NotificationHelper.getInstance().notify("StateReceiverChanged", 1102)
        }

    }
}