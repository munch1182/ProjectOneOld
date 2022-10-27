package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothDevice
import com.munch.lib.bluetooth.env.BluetoothEnv
import com.munch.lib.bluetooth.env.IBluetoothManager

/**
 * Create by munch1182 on 2022/10/27 18:02.
 */

internal class BluetoothConnector(
    private val mac: String,
    private var dev: BluetoothDevice? = null
) : IBluetoothConnector,
    IBluetoothManager by BluetoothEnv {

    private var connectState: BluetoothConnectState = BluetoothConnectState.Disconnect

    override val state: BluetoothConnectState
        get() = connectState

    override fun connect(timeout: Long) {
    }

    override fun disconnect() {
    }

    override fun addConnectListener(l: OnBluetoothConnectListener) {
    }

    override fun removeConnectListener(l: OnBluetoothConnectListener) {
    }

}