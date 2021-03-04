package com.munch.lib.ble

/**
 * Create by munch1182 on 2021/3/4 11:09.
 */
interface BtConnectListener {

    fun onStart(mac: String)

    fun connectSuccess(mac: String)

    fun connectFail(e: Exception)
}

sealed class BtConnector {

    protected var isConnecting = false
    protected var connectListener: BtConnectListener? = null

    class ClassicConnector : BtConnector() {}
    class BleConnector : BtConnector() {}

    fun setConnectListener(listener: BtConnectListener): BtConnector {
        this.connectListener = listener
        return this
    }
}

class BtConnectHelper {

}