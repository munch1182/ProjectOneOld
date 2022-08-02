package com.munch.project.one.bluetooth

import androidx.lifecycle.ViewModel
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.ScanListener
import com.munch.lib.fast.base.StateEventDispatcher
import com.munch.lib.fast.base.dispatcher
import com.munch.lib.fast.base.launch
import com.munch.lib.fast.base.toLive
import kotlinx.coroutines.flow.collectLatest

internal class BluetoothVM : ViewModel() {

    private val dis by dispatcher<BleUIState, BleIntent>(BleUIState.None)
    val dispatcher: StateEventDispatcher<BleUIState, BleIntent> = dis.toLive()

    private val ble = BluetoothHelper.instance

    init {
        launch {
            dis.event.collectLatest {
                when (it) {
                    BleIntent.StartOrStopScan -> scan()
                    BleIntent.Destroy -> ble.stop()
                }
            }
        }
    }

    private fun scan() {
        if (!ble.isScanningNow) {
            dis.update(BleUIState.None)
            ble.registerScanListener(object : ScanListener {
                override fun onStart() {
                    super.onStart()
                    dis.update(BleUIState.StartScan)
                }

                override fun onScanned(
                    dev: BluetoothDev,
                    map: LinkedHashMap<String, BluetoothDev>
                ) {
                    dis.update(BleUIState.Data(map.values.map { Dev.Ble(it) }))
                }

                override fun onComplete() {
                    super.onComplete()
                    dis.update(BleUIState.StopScan)
                }
            })
            ble.scan()
        } else {
            ble.stop()
            ble.unregisterScanListener()
        }
    }
}