package com.munch.lib.bluetooth.data

import com.munch.lib.android.extend.split
import com.munch.lib.bluetooth.connect.BluetoothGattHelper
import com.munch.lib.bluetooth.data.BluetoothDataPrintHelper.toSimpleLog
import com.munch.lib.bluetooth.helper.BluetoothHelperConfig
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Create by munch1182 on 2022/12/12 14:18.
 */
internal class BluetoothDataHelper(private val gattHelper: BluetoothGattHelper) :
    IBluetoothDataHandler, IBluetoothHelperEnv by BluetoothHelperEnv,
    BluetoothGattHelper.OnBluetoothDataReceiver {

    private val sendLock = Mutex()
    private val mac = gattHelper.mac
    private var channel = Channel<ByteArray>()
    private var dataHandler: BluetoothDataReceiver? = null

    companion object {
        private const val TAG = "data"
    }

    override val enableLog: Boolean
        get() = BluetoothHelperConfig.builder.enableLogData

    override val receive: ReceiveChannel<ByteArray>
        get() = channel

    internal fun registerDataReceive() {
        gattHelper.setDataReceiver(this)
    }

    internal fun close() {
        channel.close()
    }

    override suspend fun send(pack: ByteArray): Boolean {
        val enableLog = enableLog
        return sendLock.withLock {
            val writer = gattHelper.writer
            if (writer == null) {
                if (enableLog) log("want to SEND data but WRITER is null")
                return false
            }
            if (enableLog) log("SEND >>> [${pack.toSimpleLog()}]")
            val arrays = pack.split(gattHelper.currMtu - 3)
            for (bytes in arrays) {
                writer.value = bytes
                if (!gattHelper.writeCharacteristic(writer)) {
                    if (enableLog) log("SEND >>> fail")
                    return false
                }
            }
            if (enableLog) log("SEND >>> success")
            true
        }
    }


    override fun setDataReceiver(receiver: BluetoothDataReceiver) {
        this.dataHandler = receiver
    }

    private fun log(content: String) {
        log.log("[$TAG]: [${mac}]: $content")
    }

    private fun getDataHandler(): BluetoothDataReceiver? {
        return this.dataHandler ?: BluetoothHelperConfig.builder.receiver
    }

    override suspend fun onDataReceive(data: ByteArray) {
        channel.send(getDataHandler()?.onDataReceived(data) ?: data)
    }
}