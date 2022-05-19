package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.munch.lib.Destroyable
import com.munch.lib.OnReceive
import com.munch.lib.helper.ARSHelper
import com.munch.lib.helper.IARSHelper
import com.munch.lib.log.Logger
import com.munch.lib.receiver.ReceiverHelper

/**
 * Create by munch1182 on 2022/5/18 14:42.
 */
interface IBluetoothManager {
    val bm: BluetoothManager?
    val adapter: BluetoothAdapter?
}

interface IBluetoothState : IARSHelper<OnStateChangeListener> {
    val isSupportBle: Boolean
    val isEnable: Boolean
    val pairedDevs: Set<BluetoothDevice>?
    val connectGattDevs: List<BluetoothDevice>?
}

sealed class StateNotify {

    object Connected : StateNotify() {
        override fun toString() = "Connected"
    }

    object Disconnected : StateNotify() {
        override fun toString() = "Disconnected"
    }

    object Bonded : StateNotify() {
        override fun toString() = "Bonded"
    }

    object Bonding : StateNotify() {
        override fun toString() = "Bonding"
    }

    object BondNone : StateNotify() {
        override fun toString() = "BondNone"
    }
}

interface OnStateChangeListener {

    /**
     * @param state
     */
    fun onStateChange(state: StateNotify, mac: String? = null)
}


@SuppressLint("MissingPermission")
class BluetoothWrapper(context: Context, private val log: Logger) :
    ARSHelper<OnStateChangeListener>(),
    IBluetoothManager, IBluetoothState,
    Destroyable {

    override val bm by lazy { (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager) }
    private val receiver = BluetoothReceiver(context, this, log)
    override val adapter: BluetoothAdapter?
        get() = bm?.adapter

    override val isSupportBle: Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    override val isEnable: Boolean
        get() = adapter?.isEnabled ?: false
    override val pairedDevs: Set<BluetoothDevice>?
        get() = adapter?.bondedDevices
    override val connectGattDevs: List<BluetoothDevice>?
        get() = bm?.getConnectedDevices(BluetoothProfile.GATT)


    init {
        log.log { "broadcast register." }
        receiver.register()
    }

    class BluetoothReceiver(
        context: Context,
        private val wrapper: BluetoothWrapper,
        private val log: Logger
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
        override fun handleAction(context: Context, action: String, intent: Intent) {
            val mac =
                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.address
            log.log {
                "[$mac] broadcast receive action:${
                    action.replace("android.bluetooth.device.action.", "")
                        .replace("android.bluetooth.adapter.action.", "")
                }."
            }
            val state = when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> StateNotify.Connected
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> StateNotify.Disconnected
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
                        BluetoothAdapter.EXTRA_CONNECTION_STATE,
                        BluetoothAdapter.STATE_DISCONNECTED
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
                else -> return
            }

            wrapper.notifyUpdate { it.onStateChange(state, mac) }
        }

    }

    override fun destroy() {
        receiver.unregister()
        log.log { "broadcast unregister." }
    }

}