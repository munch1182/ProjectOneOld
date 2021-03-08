package com.munch.lib.bt

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import com.munch.lib.RequiresPermission as Permission
import com.munch.lib.helper.AddRemoveSetHelper
import com.munch.lib.helper.BluetoothStateReceiver

/**
 * 本地蓝牙实例，主要用于处理蓝牙状态
 *
 * Create by munch1182 on 2021/3/3 9:28.
 */
@Permission(
    allOf = [
        //蓝牙权限
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN]
)
class BtDeviceInstance constructor(private val context: Context) {

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
     * 该设备是否支持ble
     */
    fun isBleSupport() =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    /**
     * 蓝牙是否可用，即蓝牙是否打开
     */
    @RequiresPermission(android.Manifest.permission.BLUETOOTH)
    fun isEnable(): Boolean {
        return btAdapter?.isEnabled ?: false
    }

    fun getStateListeners(): AddRemoveSetHelper<(state: Int, turning: Boolean, available: Boolean) -> Unit> =
        stateReceiver

    fun release() {
        stateReceiver.unregister()
    }

    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun getBondedDevices(): MutableList<BtDevice> {
        return btAdapter?.bondedDevices?.map { BtDevice.from(it) }?.toMutableList()
            ?: mutableListOf()
    }
}