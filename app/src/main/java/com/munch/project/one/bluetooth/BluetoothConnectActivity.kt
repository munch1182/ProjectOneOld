package com.munch.project.one.bluetooth

import android.os.Bundle
import com.munch.lib.android.extend.bind
import com.munch.lib.android.extend.get
import com.munch.lib.bluetooth.connect.BluetoothConnectState
import com.munch.lib.bluetooth.dev.BluetoothDev
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityBluetoothConnectBinding
import com.munch.project.one.bluetooth.BluetoothIntent as INTENT
import com.munch.project.one.bluetooth.BluetoothState as STATE

/**
 * Create by munch1182 on 2022/10/27 17:21.
 */
class BluetoothConnectActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by bind<ActivityBluetoothConnectBinding>()
    private val vm by get<BluetoothVM>(BluetoothVM.SHARE_NAME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.bluetoothBtn.setOnClickListener { vm.dispatch(INTENT.ToggleConnect) }
        vm.state.observe(this) {
            when (it) {
                is STATE.ConnectDev -> showBluetooth(it.dev)
                is STATE.ShowContent -> showContent(it.any)
                else -> {}
            }
        }
    }

    private fun showContent(any: Any) {
        bind.bluetoothDesc.text = any.toString()
    }

    private fun showBluetooth(dev: BluetoothDev) {
        bind.bluetoothMac.text = dev.mac
        bind.bluetoothState.text = dev.connectState.toString().lowercase()
        dev.toScanned()?.let {
            bind.bluetoothName.text = it.name ?: "N/A"
        }
        when (dev.connectState) {
            BluetoothConnectState.Connecting,
            BluetoothConnectState.Connected -> bind.bluetoothBtn.text = "DISCONNECT"
            BluetoothConnectState.Disconnected -> bind.bluetoothBtn.text = "CONNECT"
            BluetoothConnectState.Disconnecting -> bind.bluetoothBtn.text = "WAIT"
        }
    }

}