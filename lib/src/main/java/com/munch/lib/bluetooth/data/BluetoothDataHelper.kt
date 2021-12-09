package com.munch.lib.bluetooth.data

import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.connect.Connector

/**
 * Create by munch1182 on 2021/12/9 14:48.
 */
class BluetoothDataHelper(
    private val connector: Connector,
    private val convert: IBluetoothDataConvert<Any>? = null
) : IData {

    private val logHelper = BluetoothHelper.logHelper

    private var retryCount = 1000L

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun <T> send(data: T) =
        convert?.send(data)?.let { send(it) } ?: throw UnsupportedOperationException("need convert")

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun send(byteArray: ByteArray) {
        connector.gattWrapper.send(byteArray)
    }

    override fun onReceived(received: OnByteArrayReceived) {
    }

}