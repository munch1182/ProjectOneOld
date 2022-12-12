package com.munch.lib.bluetooth.data

import com.munch.lib.android.extend.split
import com.munch.lib.android.extend.toHexStr
import com.munch.lib.bluetooth.connect.BluetoothGattHelper
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
        private const val SEP = ", "
        private const val TAG = "data"
    }

    override val receive: ReceiveChannel<ByteArray>
        get() = channel

    internal fun registerDataReceive() {

        gattHelper.setDataReceiver(this)
    }

    internal fun close() {

    }

    override suspend fun send(pack: ByteArray): Boolean {
        return sendLock.withLock {
            val writer = gattHelper.writer
            if (writer == null) {
                if (enableLog) log("want to SEND data but WRITER is null")
                return false
            }
            val arrays = pack.split(gattHelper.currMtu - 3)
            for (bytes in arrays) {
                writer.value = bytes
                if (enableLog) log("SEND: ${simpleData(pack)}")
                if (!gattHelper.writeCharacteristic(writer)) {
                    if (enableLog) log("SEND: fail")
                    return false
                }
            }
            if (enableLog) log("SEND: success")
            true
        }
    }

    private fun ByteArray.toHexStr(): String {
        return joinToString(SEP, "[", "]", transform = { it.toHexStr() })
    }

    private fun simpleData(arrays: ByteArray): String {
        if (arrays.size < 500) {
            return arrays.toHexStr()
        }
        val sb = StringBuilder("[")
        repeat(20) { sb.append(arrays[it].toHexStr()).append(SEP) }
        sb.append("...")
        val len = arrays.size
        repeat(5) { sb.append(arrays[len - 5 + it - 1].toHexStr()).append(SEP) }
        sb.append("(").append(len).append(")]")
        return sb.toString()
    }

    override fun setDataReceiver(receiver: BluetoothDataReceiver) {
        this.dataHandler = receiver
    }

    private fun log(content: String) {
        log.log("[$TAG]: [${mac}]: $content")
    }

    override suspend fun onDataReceive(data: ByteArray) {
        channel.send(this.dataHandler?.onDataReceived(data) ?: data)
    }
}