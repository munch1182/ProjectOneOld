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
import com.munch.lib.receiver.ReceiverHelper

/**
 * Create by munch1182 on 2022/5/18 14:42.
 */
interface IBluetoothManager {
    val bm: BluetoothManager?
    val adapter: BluetoothAdapter?
}

interface IBluetoothState {
    val isSupportBle: Boolean
    val isEnable: Boolean
    val isDiscovering: Boolean
    val pairedDevs: Set<BluetoothDevice>?
    val connectGattDevs: List<BluetoothDevice>?
}

@SuppressLint("MissingPermission")
class BluetoothWrapper(private val context: Context) : IBluetoothManager, IBluetoothState,
    Destroyable {

    override val bm by lazy { (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager) }
    private val receiver = BluetoothReceiver(context)
    override val adapter: BluetoothAdapter?
        get() = bm?.adapter

    override val isSupportBle: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    override val isEnable: Boolean
        get() = adapter?.isEnabled ?: false
    override val isDiscovering: Boolean
        get() = adapter?.isDiscovering ?: false
    override val pairedDevs: Set<BluetoothDevice>?
        get() = adapter?.bondedDevices
    override val connectGattDevs: List<BluetoothDevice>?
        get() = bm?.getConnectedDevices(BluetoothProfile.GATT)

    init {
        receiver.register()
    }

    class BluetoothReceiver(context: Context) :
        ReceiverHelper<OnReceive<Intent>>(
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

        }
    }

    override fun destroy() {
        receiver.unregister()
    }

}