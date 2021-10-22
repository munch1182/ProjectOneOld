package com.munch.project.one.contentobserver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.CallLog
import android.provider.Settings
import android.provider.Telephony
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.log.log
import com.munch.lib.result.with

/**
 * Create by munch1182 on 2021/10/22 09:47.
 */
class ObserverActivity : BaseBtnFlowActivity() {

    private lateinit var th: HandlerThread
    private lateinit var handler: Handler
    private val co by lazy { CallContentObserver(handler) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        th = HandlerThread("ContentObserver")
        th.start()
        handler = Handler(th.looper)
    }

    override fun getData(): MutableList<String> = mutableListOf("WX", "Call", "SMS")
    private val listener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            log("state:$state, number:$phoneNumber")
        }
    }

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> {
                val judge = {
                    NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
                }
                with(judge, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    .startOk {}
            }
            1 -> {
                with(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG)
                    .requestGrant {
                        contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, co)
                        (getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.listen(
                            listener, PhoneStateListener.LISTEN_CALL_STATE
                        )
                    }
            }
            2 -> with(Manifest.permission.READ_SMS)
                .requestGrant {
                    contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, co)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            (getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.listen(
                listener,
                PhoneStateListener.LISTEN_NONE
            )
            contentResolver.unregisterContentObserver(co)
        } catch (e: Exception) {
        }
    }
}