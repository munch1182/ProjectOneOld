package com.munch.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import com.munch.lib.OnReceive
import com.munch.lib.extend.SealedClassToString
import com.munch.lib.extend.SealedClassToStringByName
import com.munch.lib.extend.catch
import com.munch.lib.helper.ARSHelper
import com.munch.lib.helper.IARSHelper
import com.munch.lib.log.Logger
import com.munch.lib.receiver.ReceiverHelper

sealed class StateNotify : SealedClassToString by SealedClassToStringByName() {
    object StateOn : StateNotify()
    object StateOff : StateNotify()
    object Bonded : StateNotify()
    object Bonding : StateNotify()
    object BondNone : StateNotify()
}

fun interface OnStateChangeListener {
    fun onStateChange(mac: String?, state: StateNotify)
}

interface IBluetoothManager {
    val bm: BluetoothManager?
    val adapter: BluetoothAdapter?
}

interface IBluetoothState {
    val isSupportBle: Boolean
    val isEnable: Boolean
    val pairedDevs: Set<BluetoothDevice>?
    val connectGattDevs: List<BluetoothDevice>?

    fun addStateChangeListener(listener: OnStateChangeListener)
    fun removeStateChangeListener(listener: OnStateChangeListener)
}

object BluetoothEnv : ContextWrapper(null),
    IBluetoothManager, IBluetoothState {

    private val stateNotify = ARSHelper<OnStateChangeListener>()
    private val receiver by lazy { BluetoothReceiver(this, stateNotify) }

    /**
     * 在使用参数之前, 必须调用此方法
     */
    fun init(app: Context): BluetoothEnv {
        if (baseContext == null) attachBaseContext(app)
        catch { receiver.register() }
        return this
    }

    override val bm by lazy { (getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager) }
    override val adapter by lazy { bm?.adapter }

    override val isSupportBle by lazy { packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }

    override val isEnable: Boolean
        get() = adapter?.isEnabled ?: false

    override val pairedDevs: Set<BluetoothDevice>?
        get() = adapter?.bondedDevices

    override val connectGattDevs: List<BluetoothDevice>?
        get() = null

    private var scanSettings: ScanSettings? = null

    val bleScanSetting: ScanSettings
        get() = scanSettings ?: ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

    fun setBleScanSetting(scan: ScanSettings? = null) {
        scanSettings = scan
    }

    override fun addStateChangeListener(listener: OnStateChangeListener) {
        stateNotify.add(listener)
    }

    override fun removeStateChangeListener(listener: OnStateChangeListener) {
        stateNotify.remove(listener)
    }

    /**
     * 蓝牙广播监听
     */
    class BluetoothReceiver(
        context: Context,
        private val notify: IARSHelper<OnStateChangeListener>
    ) : ReceiverHelper<OnReceive<Intent>>(
        context, arrayOf(
            BluetoothDevice.ACTION_ACL_CONNECTED,
            BluetoothDevice.ACTION_ACL_DISCONNECTED,
            BluetoothDevice.ACTION_BOND_STATE_CHANGED,
            BluetoothDevice.ACTION_PAIRING_REQUEST,
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED,
            BluetoothAdapter.ACTION_STATE_CHANGED,
        )
    ) {

        private val log: Logger
            get() = BluetoothHelper.instance.log

        override fun handleAction(context: Context, action: String, intent: Intent) {
            val mac =
                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.address
            val actionStr = action.replace("android.bluetooth.device.action.", "")
                .replace("android.bluetooth.adapter.action.", "")
            if (mac != null) {
                log.log { "[$mac] broadcast receive action: $actionStr." }
            } else {
                log.log { "broadcast receive action: $actionStr." }
            }
            val state = when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> return
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val bondState = intent.getIntExtra(
                        BluetoothDevice.EXTRA_BOND_STATE,
                        BluetoothDevice.BOND_NONE
                    )
                    val bond = when (bondState) {
                        BluetoothDevice.BOND_NONE -> StateNotify.BondNone
                        BluetoothDevice.BOND_BONDED -> StateNotify.Bonded
                        BluetoothDevice.BOND_BONDING -> StateNotify.Bonding
                        else -> return
                    }
                    log.log { "[$mac] bond state change: $bond." }
                    bond
                }
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                    val connectState = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED
                    )
                    val str = when (connectState) {
                        BluetoothAdapter.STATE_DISCONNECTED -> "STATE_DISCONNECTED"
                        BluetoothAdapter.STATE_CONNECTED -> "STATE_CONNECTED"
                        BluetoothAdapter.STATE_DISCONNECTING -> "STATE_DISCONNECTING"
                        BluetoothAdapter.STATE_CONNECTING -> "STATE_CONNECTING"
                        else -> connectState.toString()
                    }
                    log.log { "[$mac] connect state change: $str." }
                    return
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF
                    )
                    val str = when (state) {
                        BluetoothAdapter.STATE_OFF -> "STATE_OFF"
                        BluetoothAdapter.STATE_TURNING_ON -> "TURNING_ON"
                        BluetoothAdapter.STATE_ON -> "STATE_ON"
                        BluetoothAdapter.STATE_TURNING_OFF -> "TURNING_OFF"
                        else -> state.toString()
                    }
                    log.log { "bluetooth state: $str." }
                    when (state) {
                        BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_ON -> return
                        //开始后才通知开启
                        BluetoothAdapter.STATE_ON -> StateNotify.StateOn
                        //关闭中即通知关闭
                        BluetoothAdapter.STATE_TURNING_OFF -> StateNotify.StateOff
                        else -> return
                    }
                }
                else -> return
            }

            notify.notifyUpdate { it.onStateChange(mac, state) }
        }

    }
}