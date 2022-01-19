package com.munch.project.one.contentobserver.handler

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import androidx.annotation.RequiresPermission
import com.munch.lib.base.PermissionHandler
import com.munch.lib.log.Logger

/**
 * Create by munch1182 on 2022/1/19 14:33.
 */
open class CallHandler(
    final override val context: Context,
    handler: Handler,
    listener: CallStateHandler.OnCallListener? = null,
    logger: Logger? = null
) : PermissionHandler, IObserver {

    private val callStateHandler = CallStateHandler(context, handler, listener, logger)
    private val callHandler = CallHandleHandler(context, logger)

    private val p =
        callHandler.permissions.toMutableList()
            .apply { addAll(callStateHandler.permissions.asList()) }
            .toTypedArray()

    override val permissions: Array<String>
        get() = p

    val notificationPolicyIntent = callHandler.notificationPolicy

    fun hadNotificationPolicy(context: Context) = callHandler.hadNotificationPolicy(context)

    override fun startObserve() {
        callStateHandler.startObserve()
    }

    override fun stopObserve() {
        callStateHandler.stopObserve()
    }

    override fun hadPermissions(): Boolean {
        return callStateHandler.hadPermissions() && callHandler.hadPermissions()
    }

    override fun onPermissionGranted() {
        super.onPermissionGranted()
        startObserve()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(
        anyOf = [Manifest.permission.ANSWER_PHONE_CALLS, Manifest.permission.MODIFY_PHONE_STATE]
    )
    fun answer() {
        callHandler.answer()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.ANSWER_PHONE_CALLS)
    fun refuse() {
        callHandler.refuse()
    }

    fun mute() {
        callHandler.mute()
    }
}