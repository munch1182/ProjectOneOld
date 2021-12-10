package com.munch.lib.bluetooth.data

import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
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

    private var retryCount = 5
        set(value) {
            field = if (value < 1) 1 else value
        }
    private var timeout: Long = 1000L

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    @WorkerThread
    fun <T> send(data: T) = convert?.send(data)?.let { send(it, timeout) } ?: false

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    @WorkerThread
    override fun send(byteArray: ByteArray, timeout: Long): Boolean {
        if (!connector.gattWrapper.canWrite) {
            logHelper.withEnable { "${connector.dev.mac}: cannot send without BluetoothGattCharacteristic." }
            return false
        }
        //重试策略
        repeat(retryCount) {
            val send = connector.gattWrapper.send(byteArray, timeout)
            if (send) {
                return send
            }
        }
        return false
    }

    override fun onReceived(received: OnByteArrayReceived) {
    }

}