@file:Suppress("DEPRECATION")

package com.munch.project.one.contentresolver

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.munch.lib.OnReceive
import com.munch.lib.extend.bind
import com.munch.lib.extend.init
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ISupportActionBar
import com.munch.lib.log.InfoStyle
import com.munch.lib.log.Logger
import com.munch.lib.log.setOnPrint
import com.munch.lib.receiver.ReceiverHelper
import com.munch.lib.task.ThreadHelper
import com.munch.lib.task.postUI
import com.munch.project.one.databinding.LayoutContentOnlyBinding
import com.permissionx.guolindev.PermissionX

/**
 * Create by munch1182 on 2022/4/16 11:06.
 */
class ContentResolverActivity : BaseFastActivity(), ISupportActionBar {

    companion object {
        internal val log = Logger("phone", infoStyle = InfoStyle.THREAD_ONLY)
    }

    private val bind by bind<LayoutContentOnlyBinding>()
    private val psr by lazy { PhoneStateReceiver(this) }
    private val tm: TelephonyManager?
        get() = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    private val pool = ThreadHelper.newCachePool()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind.init()
        log.setOnPrint { _, msg ->
            postUI {
                bind.content.text = msg
            }
        }
        start()
        PermissionX.init(this)
            .permissions(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "请求权限",
                    "请求"
                )
            }
            .request { allGranted, _, deniedList ->
                if (!allGranted) {
                    bind.content.text = "denied: ${deniedList.joinToString()}"
                }
            }
    }

    private fun stop() {
        psr.unregister()
        tm?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm?.unregisterTelephonyCallback(teleCallback)
            log.log("listener phoneState on S")
        }
    }

    private fun start() {
        psr.register()
        tm?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        log.log("register PhoneStateReceiver")
        log.log("listener PhoneStateListener")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tm?.registerTelephonyCallback(pool, teleCallback)
            log.log("listener phoneState on S")
        }

    }

    private val teleCallback = @RequiresApi(Build.VERSION_CODES.S)
    object : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            log.log("onCallStateChanged: $state")
        }
    }

    private val phoneStateListener = object : PhoneStateListener() {

        @Deprecated(
            "Deprecated in Java", ReplaceWith(
                "log.log(\"onCallStateChanged: \$state, \$phoneNumber\")",
                "com.munch.project.one.contentresolver.ContentResolverActivity.Companion.log"
            )
        )
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            log.log("onCallStateChanged: $state, $phoneNumber")
        }
    }

    class PhoneStateReceiver(context: Context) : ReceiverHelper<OnReceive<String>>(
        context,
        arrayOf("android.intent.action.PHONE_STATE")
    ) {
        override fun handleAction(context: Context, action: String, intent: Intent) {
            log.log("$action, ${intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)}")
        }

    }
}