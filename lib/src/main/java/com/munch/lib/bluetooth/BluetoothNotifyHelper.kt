package com.munch.lib.bluetooth

import com.munch.lib.base.OnChangeListener
import com.munch.lib.helper.SimpleARSHelper
import com.munch.lib.log.log

class BluetoothNotifyHelper {

    val scanListeners = SimpleARSHelper<OnScannerListener>()
    val stateListeners = SimpleARSHelper<OnStateChangeListener>()
    val connectListeners = SimpleARSHelper<OnConnectListener>()

    internal val scanCallback = object : OnScannerListener {
        override fun onStart() {
            BluetoothHelper.instance.apply {
                newState(BluetoothState.SCANNING)
                workHandler.post { scanListeners.notifyListener { it.onStart() } }
            }
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
            BluetoothHelper.instance.apply {
                newState(BluetoothState.IDLE)
                workHandler.post { scanListeners.notifyListener { it.onComplete(devices) } }
            }
        }

        override fun onFail() {
            BluetoothHelper.instance.apply {
                newState(BluetoothState.IDLE)
                workHandler.post { scanListeners.notifyListener { it.onFail() } }
            }
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

    internal val connectCallback = object : OnConnectListener {
        override fun onStart() {
            BluetoothHelper.instance.apply {
                newState(BluetoothState.CONNECTING)
                workHandler.post { connectListeners.notifyListener { it.onStart() } }
            }
        }

        override fun onConnectSuccess() {
            BluetoothHelper.instance.apply {
                newState(BluetoothState.CONNECTED)
                workHandler.post {
                    connectListeners.notifyListener { it.onConnectSuccess() }
                    clearConnectListener()
                }
            }
        }

        override fun onConnectFail() {
            BluetoothHelper.instance.apply {
                newState(BluetoothState.IDLE)
                workHandler.post {
                    connectListeners.notifyListener { it.onConnectFail() }
                    clearConnectListener()
                }
            }
        }
    }

    /**
     * 当一次连接结束后(成功/失败)，则移除监听，因为连接是个一次性的动作
     * 连接的状态不在这个回调中，这个只表示连接的动作
     */
    private fun clearConnectListener() {
        connectListeners.clear()
    }
}