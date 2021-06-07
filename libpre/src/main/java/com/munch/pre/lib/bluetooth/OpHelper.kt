package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.munch.pre.lib.base.Cancelable
import com.munch.pre.lib.base.Destroyable
import com.munch.pre.lib.helper.ThreadPoolHelper
import java.util.concurrent.ThreadPoolExecutor

/**
 * Create by munch1182 on 2021/4/27 17:29.
 */
class OpHelper : Cancelable, Destroyable {

    private val logHelp = BluetoothHelper.logHelper
    private val sender by lazy { DataSender().apply { manager.manage(this) } }
    private val reader by lazy { DataReader().apply { manager.manage(this) } }
    private val manager by lazy { LifeManager() }

    internal val characteristicListener = object : CharacteristicChangedListener {
        override fun onRead(characteristic: BluetoothGattCharacteristic?) {
            reader.onRead(characteristic)
            logHelp.withEnable { "${System.currentTimeMillis()}: onRead" }
        }

        override fun onSend(characteristic: BluetoothGattCharacteristic?, status: Int) {
            sender.onSend()
            logHelp.withEnable { "${System.currentTimeMillis()}: onSend: status: $status" }
        }
    }
    private val onReceived = object : OnReceivedListener {
        override fun onReceived(bytes: ByteArray) {
            sender.onReceived(bytes)
            BluetoothHelper.INSTANCE.apply {
                notify(receivedListeners) { it.onReceived(bytes) }
            }
        }
    }
    private val handler = BluetoothHelper.INSTANCE.handler
    private var executePool: ThreadPoolExecutor? = null
        get() {
            if (field == null) {
                field = ThreadPoolHelper.newFixThread(2)
            }
            return field
        }

    fun send(pack: SendPack) {
        handler.post { sender.send(pack) }
    }

    internal fun setGatt(gatt: BluetoothGatt) {
        sender.setGatt(gatt)
        BluetoothHelper.logHelper.withEnable { "setGatt" }
    }

    internal fun setWrite(write: BluetoothGattCharacteristic) {
        sender.setWrite(write, executePool!!)
        BluetoothHelper.logHelper.withEnable { "setWrite" }
    }

    internal fun setNotify(notify: BluetoothGattCharacteristic) {
        reader.setNotify(notify, executePool!!)
        reader.onReadListener(onReceived)
        BluetoothHelper.logHelper.withEnable { "setNotify" }
    }

    override fun cancel() {
        manager.cancel()
        executePool?.shutdownNow()
        executePool = null
    }

    override fun destroy() {
        manager.destroy()
        executePool?.shutdownNow()
        executePool = null
    }

    interface CharacteristicChangedListener {

        fun onRead(characteristic: BluetoothGattCharacteristic?)

        fun onSend(characteristic: BluetoothGattCharacteristic?, status: Int)
    }
}