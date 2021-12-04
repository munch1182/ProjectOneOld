package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
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
 * Create by munch1182 on 2021/12/3 14:52.
 */
class BluetoothEnv(private val context: Context) : Destroyable {

    private val receiver = BluetoothEnvReceiver()

    private var stateListener: OnBluetoothStateChange? = null
    private var bondStateListener: OnBluetoothStateChange? = null
    private var connectStateListener: OnBluetoothStateChange? = null

    init {
        receiver.register(context)
    }

    override fun destroy() {
        receiver.unregister(context)
    }

    /**
     * 用于监听蓝牙本身的开关
     */
    fun setStateListener(state: OnBluetoothStateChange?) {
        stateListener = state
    }

    fun setBondStateListener(bondState: OnBluetoothStateChange?) {
        bondStateListener = bondState
    }

    fun setConnectStateListener(connectState: OnBluetoothStateChange?) {
        connectStateListener = connectState
    }

    val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager?

    /**
     * 该设备是否支持蓝牙，即使用的设备是否有蓝牙模块
     */
    val isBtSupport: Boolean
        get() = adapter != null


    /**
     * 获取蓝牙操作对象，如果手机不支持，则会返回null
     */
    val adapter: BluetoothAdapter?
        get() = bm?.adapter

    /**
     * 该设备是否支持进行蓝牙广播
     */
    val isBtAdvertiserSupport: Boolean
        get() = advertiser != null

    /**
     * 获取手机广播对象，如果手机不支持，则返回null
     */
    val advertiser: BluetoothLeAdvertiser?
        get() = adapter?.bluetoothLeAdvertiser

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

    /**
     * 获取当前系统的绑定设备列表
     */
    val bondDevices: Set<BluetoothDevice>?
        @SuppressLint("InlinedApi")
        //此权限android31才有，低版本不需要此权限即可获取已绑定设备
        @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
        get() = adapter?.bondedDevices

    /**
     * 无感知的情况下打开蓝牙，不建议使用
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun enableBluetooth() = adapter?.enable()

    /**
     * 发起打开蓝牙的系统请求，需要用户同意
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun requestBluetooth() {
        context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun disableBluetooth() = adapter?.disable()

    private inner class BluetoothEnvReceiver : BroadcastReceiver() {

        private val logSystem = BluetoothHelper.logSystem

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
            val i = intent ?: return
            val dev = i.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            when (i.action) {
                //BluetoothDevice.BOND_NONE, 10
                //BluetoothDevice.BOND_BONDING, 11
                //BluetoothDevice.BOND_BONDED, 12
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val bondState = i.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    logSystem.withEnable { "BOND_STATE_CHANGED(${dev?.address}): $bondState" }
                    bondStateListener?.invoke(dev, bondState)
                }
                // BluetoothAdapter.STATE_DISCONNECTED,0
                // BluetoothAdapter.STATE_CONNECTING,1
                // BluetoothAdapter.STATE_CONNECTED,2
                // BluetoothAdapter.STATE_DISCONNECTING,3
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                    val connectState =
                        i.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)
                    logSystem.withEnable { "CONNECTION_STATE_CHANGED(${dev?.address}): $connectState" }
                    connectStateListener?.invoke(dev, connectState)
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
                    val state = i.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    logSystem.withEnable { "STATE_CHANGED(${dev?.address}): $state" }
                    stateListener?.invoke(dev, state)
                }
            }
        }
    }
}

typealias OnBluetoothStateChange = (dev: BluetoothDevice?, state: Int) -> Unit
