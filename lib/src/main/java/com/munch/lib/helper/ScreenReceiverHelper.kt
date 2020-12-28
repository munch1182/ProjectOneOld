package com.munch.lib.helper

import android.content.Context
import android.content.Intent

/**
 * Create by munch1182 on 2020/12/14 11:08.
 */
class ScreenReceiverHelper constructor(context: Context) :
    ReceiverHelper<ScreenReceiverHelper.ScreenStateListener>(
        context,
        arrayOf(Intent.ACTION_SCREEN_ON, Intent.ACTION_SCREEN_OFF, Intent.ACTION_USER_PRESENT)
    ) {

    interface ScreenStateListener {
        fun onScreenOn(context: Context?)
        fun onScreenOff(context: Context?)
        fun onUserPresent(context: Context?)
    }

    override fun handleAction(
        action: String,
        context: Context?,
        intent: Intent,
        t: ScreenStateListener
    ) {
        when (action) {
            Intent.ACTION_SCREEN_ON -> {
                t.onScreenOn(context)
            }
            Intent.ACTION_SCREEN_OFF -> {
                t.onScreenOff(context)
            }
            Intent.ACTION_USER_PRESENT -> {
                t.onUserPresent(context)
            }
        }
    }
}