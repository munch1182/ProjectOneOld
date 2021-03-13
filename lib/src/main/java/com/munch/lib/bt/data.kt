package com.munch.lib.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2021/3/5 14:46.
 */

internal sealed class BtData {

    open fun send(byteArray: ByteArray) {}
    open fun canSend(): Boolean = false
    open fun receiver(): ByteArray = byteArrayOf()
    open fun canReceiver(): Boolean = false
}

internal class BleDataHelper : BtData() {

    private var reader: BluetoothGattCharacteristic? = null
    private var writer: BluetoothGattCharacteristic? = null
    private var gatt: BluetoothGatt? = null

    private var writeFinished = true

    fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic) {
    }

    fun onCharacteristicWrite(characteristic: BluetoothGattCharacteristic?, status: Int) {
        writeFinished = true
    }

    fun setNotify(notifyService: BluetoothGattCharacteristic) {
        this.reader = notifyService
    }

    fun setWrite(writeService: BluetoothGattCharacteristic) {
        this.writer = writeService
    }

    fun setGatt(gatt: BluetoothGatt): BleDataHelper {
        this.gatt = gatt
        return this
    }

    fun release() {
        reader = null
        writer = null
        gatt = null
    }

    /**
     * 需要保证写入动作是唯一的，才能保证writeFinished是正确的
     *
     * 需要写入的值应该放入队列中
     */
    override fun send(byteArray: ByteArray) {
        super.send(byteArray)
        synchronized(this) {
            if (gatt != null && writer != null) {
                //写入值
                writer!!.value = byteArray
                gatt!!.writeCharacteristic(writer)

                //等待写入完成，最多等待100ms
                var timeout = 100L
                writeFinished = false
                try {
                    do {
                        timeout--
                        TimeUnit.MILLISECONDS.sleep(1L)
                    } while (timeout > 0 && !writeFinished)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun canReceiver() = reader == null
    override fun canSend() = writer == null
}

