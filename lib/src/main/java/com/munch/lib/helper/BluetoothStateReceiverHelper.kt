package com.munch.lib.helper

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent

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
class BluetoothStateReceiverHelper(context: Context) :
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