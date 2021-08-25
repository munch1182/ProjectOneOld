package com.munch.lib.bluetooth

import com.munch.lib.base.OnChangeListener
import com.munch.lib.helper.SimpleARSHelper

class BluetoothNotifyHelper {

    val scanListeners = SimpleARSHelper<OnScannerListener>()
    val stateListeners = SimpleARSHelper<OnStateChangeListener>()

    internal val scanCallback = object : OnScannerListener {
        override fun onStart() {
            BluetoothHelper.instance.newState(BluetoothState.SCANNING)
            BluetoothHelper.instance.workHandler.post { scanListeners.notifyListener { it.onStart() } }
        }

        override fun onScan(device: BtDevice) {
            BluetoothHelper.instance.workHandler.post {
                scanListeners.notifyListener {
                    it.onScan(device)
                }
            }
        }

        override fun onBatchScan(devices: MutableList<BtDevice>) {
            BluetoothHelper.instance.workHandler.post {
                scanListeners.notifyListener {
                    it.onBatchScan(devices)
                }
            }
        }

        override fun onComplete(devices: MutableList<BtDevice>) {
            BluetoothHelper.instance.newState(BluetoothState.IDLE)
            BluetoothHelper.instance.workHandler.post {
                scanListeners.notifyListener {
                    it.onComplete(devices)
                }
            }
        }

        override fun onFail() {
            BluetoothHelper.instance.newState(BluetoothState.IDLE)
            BluetoothHelper.instance.workHandler.post { scanListeners.notifyListener { onFail() } }
        }
    }

    internal val stateChangeCallback = object : OnChangeListener {
        override fun onChange() {
            BluetoothHelper.instance.workHandler.post {
                stateListeners.notifyListener {
                    it.onChange(BluetoothHelper.instance.state.currentState)
                }
            }
        }
    }
}