package com.munch.lib.bluetooth.data

import com.munch.lib.android.extend.UpdateJob
import com.munch.lib.android.extend.split
import com.munch.lib.android.helper.ARSHelper
import com.munch.lib.bluetooth.connect.BluetoothGattHelper
import com.munch.lib.bluetooth.data.BluetoothDataLogHelper.toSimpleLog
import com.munch.lib.bluetooth.helper.BluetoothHelperConfig
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Create by munch1182 on 2022/12/12 14:18.
 */
internal class BluetoothLeDataHelper(private val gattHelper: BluetoothGattHelper) :
    IBluetoothDataManger, IBluetoothHelperEnv by BluetoothHelperEnv,
    ARSHelper<BluetoothDataReceiver>() {

    private val sendLock = Mutex()
    private val mac = gattHelper.mac
    private var dataHandler: BluetoothDataReceiver? = null
    private var active = false
    private var job = UpdateJob()

    companion object {
        private const val TAG = "data"
    }

    override val enableLog: Boolean
        get() = BluetoothHelperConfig.config.enableLogData

    // todo 发送序列
    override suspend fun send(pack: ByteArray): Boolean {
        return withContext(job.currOrCanceled) {
            val enableLog = enableLog
            sendLock.withLock {
                val writer = gattHelper.writer
                if (writer == null) {
                    if (enableLog) log("want to SEND data but WRITER is null")
                    return@withContext false
                }
                if (enableLog) log("SEND >>> [${pack.toSimpleLog()}]")
                val arrays = pack.split(gattHelper.currMtu - 3)
                for (bytes in arrays) {
                    if (!active) return@withContext false // 如果正在发送多包数据时, 需要跳出避免堵塞
                    writer.value = bytes
                    if (!gattHelper.writeCharacteristic(writer)) {
                        if (enableLog) log("SEND >>> fail")
                        return@withContext false
                    }
                }
                if (enableLog) log("SEND >>> success")
                true
            }
        }
    }

    override fun cancelSend() {
        job.cancel()
    }

    override fun active() {
        active = true
        job.new()
        log("ACTIVE DataHandler")
        gattHelper.setDataReceiver {
            getDataHandler()?.onReceived(it)
            launch { update { h -> runBlocking { h.onReceived(it) } } }
        }
    }

    override fun inactive() {
        active = false
        job.cancel()
        log("INACTIVE DataHandler")
        gattHelper.setDataReceiver(null)
    }

    override fun addReceiver(receiver: BluetoothDataReceiver) {
        add(receiver)
    }

    override fun removeReceiver(receiver: BluetoothDataReceiver) {
        remove(receiver)
    }

    override fun setReceiver(receiver: BluetoothDataReceiver?) {
        this.dataHandler = receiver
    }

    private fun log(content: String) {
        log.log("[$TAG]: [${mac}]: $content")
    }

    private fun getDataHandler(): BluetoothDataReceiver? {
        return this.dataHandler ?: BluetoothHelperConfig.config.receiver
    }
}