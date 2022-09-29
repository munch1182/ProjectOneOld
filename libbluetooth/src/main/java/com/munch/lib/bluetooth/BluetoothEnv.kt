package com.munch.lib.bluetooth

import android.bluetooth.BluetoothAdapter as ADAPTER
import android.bluetooth.BluetoothDevice as DEV
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.lazy
import com.munch.lib.android.extend.toOrNull
import com.munch.lib.android.helper.ARSHelper
import com.munch.lib.android.helper.ReceiverHelper
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/9/29 9:30.
 */
object BluetoothEnv : IBluetoothManager, IBluetoothState {
    private val context: Context = AppHelper
    override val bm: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE).toOrNull()
    }
    override val adapter: ADAPTER? by lazy { bm?.adapter }
    override val isSupportBle: Boolean by lazy {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
    override val isEnable: Boolean
        get() = adapter?.isEnabled ?: false
    override val pairedDevs: Set<DEV>?
        get() = adapter?.bondedDevices
    private val stateNotify = ARSHelper<OnBluetoothStateNotifyListener?>()

    override fun addStateChangeListener(l: OnBluetoothStateNotifyListener?) {
        stateNotify.add(l)
    }

    override fun removeStateChangeListener(l: OnBluetoothStateNotifyListener?) {
        stateNotify.remove(l)
    }

    init {
        BluetoothReceiver().register()
    }

    /**
     * 蓝牙广播监听
     */
    class BluetoothReceiver : ReceiverHelper<OnBluetoothStateNotifyListener?>(
        arrayOf(
            DEV.ACTION_ACL_CONNECTED,
            DEV.ACTION_ACL_DISCONNECTED,
            DEV.ACTION_BOND_STATE_CHANGED,
            DEV.ACTION_PAIRING_REQUEST,
            ADAPTER.ACTION_CONNECTION_STATE_CHANGED,
            ADAPTER.ACTION_STATE_CHANGED,
        )
    ), IBluetoothFun {

        override fun dispatchAction(context: Context, action: String, intent: Intent) {
            launch {
                val mac = intent.getParcelableExtra<DEV>(DEV.EXTRA_DEVICE)?.address
                val actionStr = action.simpleAction()
                if (mac != null) {
                    log.log("[$mac] broadcast receive action: $actionStr.")
                } else {
                    log.log("broadcast receive action: $actionStr.")
                }
                val notify: BluetoothStateNotify = when (action) {
                    ADAPTER.ACTION_CONNECTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(ADAPTER.EXTRA_CONNECTION_STATE, -1)
                        log.log("[$mac] connect state change: ${state.connectStateFmt()}.")
                        return@launch
                    }
                    DEV.ACTION_BOND_STATE_CHANGED -> {
                        val state = intent.getIntExtra(DEV.EXTRA_BOND_STATE, -1)
                        log.log("[$mac] bond state change: ${state.bondStateFmt()}.")
                        when (state) {
                            DEV.BOND_NONE -> BluetoothStateNotify.BondFail
                            DEV.BOND_BONDING -> BluetoothStateNotify.Bonding
                            DEV.BOND_BONDED -> BluetoothStateNotify.Bonded
                            else -> return@launch
                        }
                    }
                    ADAPTER.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(ADAPTER.EXTRA_STATE, -1)
                        log.log("bluetooth state: ${state.stateFmt()}.")
                        when (state) {
                            ADAPTER.STATE_ON -> BluetoothStateNotify.StateOn
                            ADAPTER.STATE_TURNING_OFF -> BluetoothStateNotify.StateOff
                            else -> return@launch
                        }
                    }
                    else -> return@launch
                }
                update { it?.onStateNotify(notify, mac) }
            }
        }

        private fun Int.connectStateFmt(): String {
            return when (this) {
                ADAPTER.STATE_DISCONNECTED -> "DISCONNECTED"
                ADAPTER.STATE_DISCONNECTING -> "DISCONNECTING"
                ADAPTER.STATE_CONNECTED -> "CONNECTED"
                ADAPTER.STATE_CONNECTING -> "CONNECTING"
                else -> toString()
            }
        }

        private fun Int.stateFmt(): String {
            return when (this) {
                ADAPTER.STATE_OFF -> "STATE_OFF"
                ADAPTER.STATE_TURNING_OFF -> "TURNING_OFF"
                ADAPTER.STATE_ON -> "STATE_ON"
                ADAPTER.STATE_TURNING_ON -> "TURNING_ON"
                else -> toString()
            }
        }

        private fun Int.bondStateFmt(): String {
            return when (this) {
                DEV.BOND_BONDED -> "BOND_BONDED"
                DEV.BOND_BONDING -> "BOND_BONDING"
                DEV.BOND_NONE -> "BOND_NONE"
                else -> toString()
            }
        }

        private fun String.simpleAction(): String {
            return replace("android.bluetooth.device.action.", "")
                .replace("android.bluetooth.adapter.action.", "")
        }

    }

}