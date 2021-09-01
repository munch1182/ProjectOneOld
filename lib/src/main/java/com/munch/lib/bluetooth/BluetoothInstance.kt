package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.munch.lib.base.Destroyable

/**
 * 用以检查手机的蓝牙相关
 *
 * Create by munch1182 on 2021/8/17 9:59.
 */
class BluetoothInstance(private val context: Context) : Destroyable {

    private val btReceiver = BluetoothInstanceReceiver()

    val manager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    init {
        btReceiver.register(context)
    }

    /**
     * 用于监听蓝牙本身的开关
     */
    fun setStateListener(state: ((state: Int) -> Unit)?) {
        btReceiver.setStateListener(state)
    }

    fun setBondStateListener(bondState: ((state: Int, dev: BluetoothDevice?) -> Unit)?) {
        btReceiver.setBondStateListener(bondState)
    }

    fun setConnectStateListener(connectState: ((state: Int, dev: BluetoothDevice?) -> Unit)?) {
        btReceiver.setConnectStateListener(connectState)
    }

    /**
     * 获取蓝牙操作对象，如果手机不支持，则返回null
     */
    val adapter: BluetoothAdapter?
        get() = manager?.adapter

    /**
     * 获取手机广播对象，如果手机不支持，则返回null
     */
    val advertiser: BluetoothLeAdvertiser?
        get() = adapter?.bluetoothLeAdvertiser

    /**
     * 该设备是否支持蓝牙，即使用的设备是否有蓝牙模块
     */
    val isBtSupport: Boolean
        get() = adapter != null

    /**
     * 该设备是否支持进行蓝牙广播
     */
    val isBtAdvertiserSupport: Boolean
        get() = advertiser != null

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

    val isLe2MPhySupported: Boolean
        @RequiresApi(Build.VERSION_CODES.O)
        get() = adapter?.isLe2MPhySupported ?: false

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
     * 获取当前连接状态
     *
     * BluetoothAdapter.STATE_DISCONNECTED,0
     * BluetoothAdapter.STATE_CONNECTING,1
     * BluetoothAdapter.STATE_CONNECTED,2
     * BluetoothAdapter.STATE_DISCONNECTING,3
     */
    @SuppressLint("DiscouragedPrivateApi")
    fun getConnectedState(): Int? {
        return try {
            val connectState = BluetoothAdapter::class.java.getDeclaredMethod("getConnectionState")
            connectState.isAccessible = true
            connectState.invoke(adapter) as? Int
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
        btReceiver.unregister(context)
    }
}

class BluetoothInstanceReceiver : BroadcastReceiver() {

    private val logSystem = BluetoothHelper.logSystem
    private var stateListener: ((state: Int) -> Unit)? = null
    private var bondStateListener: ((state: Int, dev: BluetoothDevice?) -> Unit)? = null
    private var connectStateListener: ((state: Int, dev: BluetoothDevice?) -> Unit)? = null

    fun register(context: Context) {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            /*addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)*/
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        })
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }

    fun setStateListener(state: ((state: Int) -> Unit)?) {
        this.stateListener = state
    }

    fun setBondStateListener(bondState: ((state: Int, dev: BluetoothDevice?) -> Unit)?) {
        this.bondStateListener = bondState
    }

    fun setConnectStateListener(connectState: ((state: Int, dev: BluetoothDevice?) -> Unit)?) {
        connectStateListener = connectState
    }

    /**
     * 绑定设备广播顺序:
     * 1. [BluetoothDevice.ACTION_BOND_STATE_CHANGED]
     * 2. [BluetoothDevice.ACTION_ACL_CONNECTED]
     * 3. [BluetoothDevice.ACTION_BOND_STATE_CHANGED]
     * 4. [BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED]
     * 5. [BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED]
     * 移除绑定广播顺序：
     * 1. [BluetoothDevice.ACTION_ACL_DISCONNECTED]
     * 2. [BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED]
     * 3. [BluetoothDevice.ACTION_BOND_STATE_CHANGED]]
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        when (intent.action) {
            //BluetoothDevice.BOND_NONE, 10
            //BluetoothDevice.BOND_BONDING, 11
            //BluetoothDevice.BOND_BONDED, 12
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                val dev = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                logSystem.withEnable { "BOND_STATE_CHANGED(${dev?.address}): $bondState" }
                if (bondState != -1) {
                    bondStateListener?.invoke(bondState, dev)
                }
            }
            // BluetoothAdapter.STATE_DISCONNECTED,0
            // BluetoothAdapter.STATE_CONNECTING,1
            // BluetoothAdapter.STATE_CONNECTED,2
            // BluetoothAdapter.STATE_DISCONNECTING,3
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                val connectState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)
                val dev = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                logSystem.withEnable { "CONNECTION_STATE_CHANGED(${dev?.address}): $connectState" }
                if (connectState != -1) {
                    connectStateListener?.invoke(connectState, dev)
                }
            }
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
            }
            //BluetoothAdapter.STATE_OFF, 10
            //BluetoothAdapter.STATE_TURNING_ON, 11
            //BluetoothAdapter.STATE_ON, 12
            //BluetoothAdapter.STATE_TURNING_OFF, 13
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                logSystem.withEnable { "STATE_CHANGED: $state" }
                if (state != -1) {
                    this.stateListener?.invoke(state)
                }
            }
        }
    }
}