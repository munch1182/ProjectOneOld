package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import com.munch.lib.base.Destroyable
import com.munch.lib.helper.receiver.BluetoothStateReceiver
import com.munch.lib.helper.receiver.ReceiverHelper
import com.munch.lib.log.log

/**
 * 用以检查手机的蓝牙相关
 *
 * Create by munch1182 on 2021/8/17 9:59.
 */
class BluetoothInstance(private val context: Context) : Destroyable {

    private val btStateReceiver = BluetoothStateReceiver(context)
    private val btReceiver = BluetoothInstanceReceiver(context)
    private val manager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    init {
        btStateReceiver.register()
        btReceiver.register()
    }

    /**
     * 用于监听蓝牙本身的开关
     */
    fun setStateListener(state: (state: Int, turning: Boolean, available: Boolean) -> Unit) {
        btStateReceiver.add(state)
    }

    /**
     * 获取蓝牙操作对象，如果手机不支持，则返回null
     */
    val adapter: BluetoothAdapter?
        get() = manager?.adapter

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
    fun getBondedDevices(): MutableList<BluetoothDev>? {
        return adapter?.bondedDevices?.map { BluetoothDev.from(it) }?.toMutableList()

    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_ADMIN)
    fun enable() = adapter?.enable()

    /**
     * 用于获取该蓝牙设备是否处于gatt连接状态，如果蓝牙已关闭、未获取到也会返回false
     */
    @RequiresPermission(android.Manifest.permission.BLUETOOTH)
    fun isConnectedByGatt(dev: BluetoothDev): Boolean {
        return manager?.getConnectionState(dev.dev, BluetoothProfile.GATT) ==
                BluetoothProfile.STATE_CONNECTED
    }

    /**
     * 获取已经连接的蓝牙设备
     *
     *  @return 获取当前已经连接的蓝牙设备，如果蓝牙已关闭，或获取失败，或者没有连接的设备，则返回null
     */
    @RequiresPermission(android.Manifest.permission.BLUETOOTH)
    fun getConnectedDevice(): MutableList<BluetoothDev>? {
        return manager?.getConnectedDevices(BluetoothProfile.GATT)?.map { BluetoothDev.from(it) }
            ?.toMutableList()
    }

    override fun destroy() {
        btStateReceiver.clear()
        btStateReceiver.unregister()
    }
}

class BluetoothInstanceReceiver(context: Context) : ReceiverHelper<(type: Int) -> Unit>(
    context,
    arrayOf(
        BluetoothDevice.ACTION_PAIRING_REQUEST,
        BluetoothDevice.ACTION_BOND_STATE_CHANGED,
        BluetoothDevice.ACTION_ACL_CONNECTED,
        BluetoothDevice.ACTION_ACL_DISCONNECTED,
        BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED
    )
) {
    companion object {
        const val TYPE_BOND_STATE_CHANGE = 0
        const val TYPE_CONNECT_STATE_CHANGE = 0
    }

    override fun handleAction(
        action: String,
        context: Context?,
        intent: Intent,
        t: (type: Int) -> Unit
    ) {
        log(action)
    }
}