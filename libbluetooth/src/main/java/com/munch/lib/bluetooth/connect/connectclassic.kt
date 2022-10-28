package com.munch.lib.bluetooth.connect

import com.munch.lib.android.extend.lazy
import com.munch.lib.bluetooth.env.BluetoothEnv
import com.munch.lib.bluetooth.env.IBluetoothManager
import com.munch.lib.bluetooth.env.IBluetoothState
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/10/29 0:08.
 */

internal class BluetoothClassicService :
    IBluetoothState by BluetoothEnv,
    IBluetoothManager by BluetoothEnv,
    IBluetoothHelperEnv by BluetoothHelperEnv {

    private val serviceSocket by lazy {
        val name = BluetoothConnectorConfig.builder.name
        val uuid = BluetoothConnectorConfig.builder.uuid
        adapter?.listenUsingInsecureRfcommWithServiceRecord(name, uuid)
    }

    fun start() {
        if (isEnable) {
            return
        }
        launch {
            log("1")
            serviceSocket?.accept()
            log("2")
        }
    }
}