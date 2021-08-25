package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission

/**
 * 用以检查手机的蓝牙相关
 *
 * Create by munch1182 on 2021/8/17 9:59.
 */
class BluetoothInstance(private val context: Context) {

    /**
     * 获取蓝牙操作对象，如果手机不支持，则返回null
     */
    val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    /**
     * 该设备是否支持蓝牙，即使用的设备是否有蓝牙模块
     */
    val isBtSupport: Boolean
        get() = adapter != null

    /**
     * 该设备是否支持ble
     */
    val isBleSupport: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    /**
     * 是否支持批处理扫描
     *
     * 如果支持，可以在扫描中设置[android.bluetooth.le.ScanSettings.Builder.setReportDelay]大于0
     * 则会回调[android.bluetooth.le.ScanCallback.onBatchScanResults]
     */
    val isScanBatchingSupported: Boolean
        get() = adapter?.isOffloadedScanBatchingSupported ?: false

    /**
     * 蓝牙是否可用，即蓝牙是否打开
     */
    val isEnable: Boolean
        @RequiresPermission(android.Manifest.permission.BLUETOOTH)
        get() = adapter?.isEnabled ?: false

    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun getBondedDevices(type: BluetoothType = BluetoothType.Ble): MutableList<BtDevice> {
        return adapter?.bondedDevices
            ?.map { BtDevice.from(it, type) }?.toMutableList()
            ?: mutableListOf()
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_ADMIN)
    fun enable() = adapter?.enable()

    /**
     * 获取当前手机的连接状态
     *
     * @return 获取当前的连接状态，如果蓝牙已关闭，或获取失败，则返回null，否则返回状态值
     *
     * @see BluetoothAdapter.STATE_CONNECTED
     * @see BluetoothAdapter.STATE_DISCONNECTED
     * @see BluetoothAdapter.STATE_CONNECTING
     * @see BluetoothAdapter.STATE_DISCONNECTING
     */
    @SuppressLint("DiscouragedPrivateApi")
    fun getConnectedState(): Int? {
        try {
            val adapter = BluetoothHelper.instance.set.adapter ?: return null
            val connectionState = adapter.javaClass.getDeclaredMethod("getConnectionState")
            connectionState.isAccessible = true
            return connectionState.invoke(adapter) as? Int? ?: return null
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 获取已经连接的蓝牙设备
     *
     *  @return 获取当前已经连接的蓝牙设备，如果蓝牙已关闭，或获取失败，或者没有连接的设备，则返回null
     *
     * @see getConnectedState
     */
    @RequiresPermission(android.Manifest.permission.BLUETOOTH)
    fun getConnectedDevice(): BtDevice? {
        try {
            val adapter = BluetoothHelper.instance.set.adapter ?: return null
            val isConnected = BluetoothDevice::class.java.getDeclaredMethod("isConnected")
            isConnected.isAccessible = true
            adapter.bondedDevices.forEach {
                if (isConnected.invoke(it) as? Boolean? == true) {
                    return BtDevice.from(it)
                }
            }
        } catch (e: Exception) {
            //ignore
        }
        return null
    }
}