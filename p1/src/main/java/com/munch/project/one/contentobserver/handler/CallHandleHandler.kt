package com.munch.project.one.contentobserver.handler

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.android.internal.telephony.ITelephony
import com.munch.lib.base.PermissionHandler
import com.munch.lib.log.Logger

/**
 * 用于处理接听、挂断、静音来电
 *
 * Create by munch1182 on 2022/1/19 13:53.
 */
@Suppress("DEPRECATION")
open class CallHandleHandler(
    override val context: Context,
    private val logger: Logger?
) : PermissionHandler {

    private val tm by lazy { context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager? }
    private val am by lazy { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager? }
    override val permissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ANSWER_PHONE_CALLS
            )
        } else {
            arrayOf(Manifest.permission.CALL_PHONE)
        }

    //跳转免打扰权限
    val notificationPolicy = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    override fun hadPermissions(): Boolean {
        return super.hadPermissions() && hadNotificationPolicy(context)
    }

    fun hadNotificationPolicy(context: Context): Boolean {
        return try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?
            nm?.isNotificationPolicyAccessGranted ?: true
        } catch (e: Exception) {
            logger?.log("fail：isNotificationPolicyAccessGranted()")
            logger?.log(e)
            true
        }
    }

    //MODIFY_PHONE_STATE权限很可能会被直接拒绝，建议申请ANSWER_PHONE_CALLS
    @SuppressLint("InlinedApi")
    @RequiresPermission(
        anyOf = [Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.MODIFY_PHONE_STATE]
    )
    fun answer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            answerO()
        } else {
            answerL()
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.ANSWER_PHONE_CALLS)
    fun refuse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            refuseP()
        } else {
            refuseL()
        }
    }

    fun mute() {
        try {
            logger?.log("execute: mute")
            //Not allowed to change Do Not Disturb state异常，需要申请ACCESS_NOTIFICATION_POLICY权限
            am?.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (e: Exception) {
            logger?.log("fail：mute()")
            logger?.log(e)
        }
    }

    @SuppressLint("PrivateApi")
    private fun refuseL() {
        try {
            logger?.log("execute: refuseL")
            val method = Class.forName("android.os.ServiceManager").getMethod(
                "getService",
                String::class.java
            )
            val binder =
                method.invoke(null, Context.TELEPHONY_SERVICE) as IBinder
            val telephony = ITelephony.Stub.asInterface(binder)
            telephony.endCall()
        } catch (e: Exception) {
            logger?.log("fail：refuseL()")
            logger?.log(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @RequiresPermission(Manifest.permission.ANSWER_PHONE_CALLS)
    private fun refuseP() {
        try {
            logger?.log("execute: refuseP")
            tm?.endCall()
        } catch (e: Exception) {
            logger?.log("fail：refuseP()")
            logger?.log(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(
        anyOf = [Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.MODIFY_PHONE_STATE]
    )
    private fun answerO() {
        try {
            logger?.log("execute：answerO()")
            tm?.acceptRingingCall()
        } catch (e: Exception) {
            logger?.log("fail：answerO()")
            logger?.log(e)
        }
    }

    private fun answerL() {
        logger?.log("execute：answerL()")
        try {
            val enforcedPerm = "android.permission.CALL_PRIVILEGED"
            val btnDown = Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                Intent.EXTRA_KEY_EVENT, KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_HEADSETHOOK
                )
            )
            val btnUp = Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                Intent.EXTRA_KEY_EVENT, KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_HEADSETHOOK
                )
            )
            context.sendOrderedBroadcast(btnDown, enforcedPerm)
            context.sendOrderedBroadcast(btnUp, enforcedPerm)
        } catch (e: Exception) {
            logger?.log("fail：answerL()")
            logger?.log(e)
        }
    }

}