package com.munch.lib.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.munch.lib.RequiresPermission
import com.munch.lib.helper.AddRemoveSetHelper
import com.munch.lib.helper.BluetoothStateReceiver

/**
 * 本地蓝牙实例，主要用于处理蓝牙状态
 *
 * Create by munch1182 on 2021/3/3 9:28.
 */
@RequiresPermission(
    allOf = [
        //蓝牙权限
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN]
)
@SuppressLint("MissingPermission")
class BtDeviceInstance constructor(context: Context) {

    internal val btAdapter = BluetoothAdapter.getDefaultAdapter()
    private val stateReceiver = BluetoothStateReceiver(context)
    private var btState = BluetoothAdapter.STATE_OFF

    init {
        stateReceiver.add { btState, _, _ -> this.btState = btState }
        stateReceiver.register()
    }

    /**
     * 该设备是否支持蓝牙，即使用的设备是否有蓝牙模块
     */
    fun isBtSupport() = btAdapter != null

    /**
     * 蓝牙是否可用，即蓝牙是否打开
     */
    fun isEnable(): Boolean {
        return btAdapter?.isEnabled ?: false
    }

    fun getStateListeners(): AddRemoveSetHelper<(state: Int, turning: Boolean, available: Boolean) -> Unit> =
        stateReceiver

    fun release() {
        stateReceiver.unregister()
    }
}