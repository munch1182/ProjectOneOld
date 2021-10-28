package com.munch.project.one.contentobserver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.CallLog
import android.provider.Telephony
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.databinding.ItemLogContentBinding
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.lib.log.log
import com.munch.lib.notification.NotificationHelper
import com.munch.lib.notification.NotificationServiceHelper
import com.munch.lib.result.with
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityObserverBinding
import com.munch.project.one.notification.NotificationService

/**
 * Create by munch1182 on 2021/10/22 09:47.
 */
class ObserverActivity : BaseBigTextTitleActivity() {

    private lateinit var th: HandlerThread
    private lateinit var handler: Handler
    private val co by lazy { CallContentObserver(handler) }

    private val bind by bind<ActivityObserverBinding>()

    private val tm: TelephonyManager?
        get() = (getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)
    private val strAdapter by lazy {
        SimpleAdapter<String, ItemLogContentBinding>(R.layout.item_log_content) { _, bd, str ->
            bd.text = str
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.observerBtnNotification.setOnClickListener { enableNotification() }
        bind.observerBtnCall.setOnClickListener { enableCall() }
        bind.observerBtnSms.setOnClickListener { enableSms() }

        bind.observerRv.apply {
            layoutManager = LinearLayoutManager(this@ObserverActivity)
            adapter = strAdapter
        }

        NotificationHelper.connectedStateChanges.set(this) {
            showNotificationState(it)
            strAdapter.add("state change: $it")
        }
        NotificationHelper.notificationChanges.set(this) { sbn, isPosted ->
            if (isPosted) {
                val extras = sbn.notification.extras
                strAdapter.add(
                    "${sbn.packageName} ${sbn.id}: title: ${extras.getString(Notification.EXTRA_TITLE)}," +
                            " content: ${extras.getString(Notification.EXTRA_TEXT)}, tag:${sbn.tag} posted"
                )
            } else {
                strAdapter.add("${sbn.id} removed")
            }
        }

        th = HandlerThread("ContentObserver")
        th.start()
        handler = Handler(th.looper)

        showNotificationState(NotificationHelper.isConnected)
    }

    @SuppressLint("SetTextI18n")
    private fun showNotificationState(it: Boolean) {
        bind.observerBtnNotificationState.text = "state:${if (it) "enable" else "disable"}"
    }

    private fun enableSms() {
        with(Manifest.permission.READ_SMS)
            .requestGrant {
                contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, co)
            }
    }

    private fun enableCall() {
        with(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG)
            .requestGrant {
                contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, co)
                tm?.listen(psListener, PhoneStateListener.LISTEN_CALL_STATE)
            }
    }

    private fun enableNotification() {
        with(
            { NotificationServiceHelper.isEnable() }, NotificationServiceHelper.requestIntent()
        ).startOk {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotificationServiceHelper.enable(cls = NotificationService::class.java)
            }
        }
    }

    private val psListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            log("state:$state, number:$phoneNumber")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotificationServiceHelper.disable(cls = NotificationService::class.java)
            }
            tm?.listen(psListener, PhoneStateListener.LISTEN_NONE)
            contentResolver.unregisterContentObserver(co)
        } catch (e: Exception) {
        }
    }
}