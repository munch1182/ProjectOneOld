package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import com.munch.pre.lib.PERMISSIONS
import com.munch.pre.lib.helper.ARSHelper
import com.munch.pre.lib.helper.receiver.BluetoothStateReceiver

/**
 * Create by munch1182 on 2021/4/8 11:01.
 */
@PERMISSIONS(
    allOf = [
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN]
)
class BtDeviceInstance(private val context: Context) {

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

    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun getBondedDevices(): MutableList<BtDevice> {
        return btAdapter?.bondedDevices?.map { BtDevice.from(it) }?.toMutableList()
            ?: mutableListOf()
    }

    fun getBtStateListeners(): ARSHelper<(state: Int, turning: Boolean, available: Boolean) -> Unit> =
        stateReceiver
}