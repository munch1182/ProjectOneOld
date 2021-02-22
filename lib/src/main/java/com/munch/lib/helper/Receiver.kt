@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Create by munch1182 on 2020/12/28 21:17.
 */
/**
 * 收听屏幕状态广播
 *
 * 注意：有的手机不在前台收不到此广播
 *
 * Create by munch1182 on 2020/12/14 11:08.
 */
open class ScreenReceiver constructor(context: Context) :
    ReceiverHelper<ScreenReceiver.ScreenStateListener>(
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

/**
 *
 * 收听蓝牙状态广播
 *
 * (turning: Boolean,available: Boolean):turning表示是否处于turning状态，available表示回调的状态
 *
 * [BluetoothAdapter.STATE_TURNING_OFF]用来关闭蓝牙连接，而连接蓝牙则需要等到[BluetoothAdapter.STATE_ON]之后
 *
 * Create by munch1182 on 2020/12/28 16:57.
 */
open class BluetoothStateReceiver(context: Context) :
    ReceiverHelper<(turning: Boolean, available: Boolean) -> Unit>(
        context,
        arrayOf(BluetoothAdapter.ACTION_STATE_CHANGED)
    ) {

    override fun handleAction(
        action: String,
        context: Context?,
        intent: Intent,
        t: (turning: Boolean, available: Boolean) -> Unit
    ) {
        val bleState: Int = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1).takeIf {
            it != -1
        } ?: return
        when (bleState) {
            BluetoothAdapter.STATE_TURNING_ON -> {
                t.invoke(true, true)
            }
            BluetoothAdapter.STATE_ON -> {
                t.invoke(false, true)
            }
            BluetoothAdapter.STATE_OFF -> {
                t.invoke(false, false)
            }

            BluetoothAdapter.STATE_TURNING_OFF -> {
                t.invoke(true, false)
            }

        }
    }


}

/**
 * 应用安装、卸载广播
 *
 * 在android11，需要[android.Manifest.permission.QUERY_ALL_PACKAGES]权限
 */
open class AppInstallReceiver(context: Context) :
    ReceiverHelper<(context: Context?, isAdd: Boolean, pkgName: String?) -> Unit>(
        context,
        arrayOf(Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_ADDED)
    ) {

    override fun buildIntentFilter(intent: IntentFilter) {
        super.buildIntentFilter(intent)
        intent.addDataScheme("package")
    }

    override fun handleAction(
        action: String,
        context: Context?,
        intent: Intent,
        t: (context: Context?, isAdd: Boolean, pkgName: String?) -> Unit
    ) {
        when (action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                t.invoke(context, true, intent.dataString)
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                t.invoke(context, false, intent.dataString)
            }
        }
    }
}