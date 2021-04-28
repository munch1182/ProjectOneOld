package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.munch.pre.lib.base.Cancelable
import com.munch.pre.lib.base.Destroyable
import com.munch.pre.lib.helper.ARSHelper
import com.munch.pre.lib.helper.ThreadPoolHelper
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.lang.Exception
import java.util.*
import java.util.concurrent.ThreadPoolExecutor

/**
 * Create by munch1182 on 2021/4/27 17:29.
 */
class OpHelper : Cancelable, Destroyable {

    private val logHelp = BluetoothHelper.logHelper
    val characteristicListener = object : CharacteristicChangedListener {
        override fun onRead(characteristic: BluetoothGattCharacteristic?) {
            reader.onReadListener(characteristic)
            logHelp.withEnable { "${System.currentTimeMillis()}: onRead" }
        }

        override fun onSend(characteristic: BluetoothGattCharacteristic?, status: Int) {
            sender.onSend(characteristic)
            logHelp.withEnable { "${System.currentTimeMillis()}: onSend: status: $status" }
        }
    }
    private val sender by lazy { DataSender() }
    private val reader by lazy { DataReader() }
    private val onReceived = object : OnReceivedListener {
        override fun onReceived(bytes: ByteArray) {
            BluetoothHelper.INSTANCE.notify(receivedListeners) { it.onReceived(bytes) }
        }
    }
    val receivedListeners = object : ARSHelper<OnReceivedListener>() {}
    private val maxPackSize = BluetoothHelper.INSTANCE.config.mtu
    private val handler = BluetoothHelper.INSTANCE.handler
    private var executePool: ThreadPoolExecutor? = null
        get() {
            if (field == null) {
                field = ThreadPoolHelper.newFixThread()
            }
            return field
        }

    //sendListener
    fun send(bytes: ByteArray) {
        handler.post {
            if (bytes.size > maxPackSize) {
                //
                sender.send(bytes)
            } else {
                sender.send(bytes)
            }
        }
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
        reader.cancel()
        sender.cancel()
        executePool?.shutdownNow()
        executePool = null
    }

    override fun destroy() {
        reader.destroy()
        sender.destroy()
        executePool?.shutdownNow()
        executePool = null
    }

    internal class DataReader : Cancelable, Destroyable {
        private var notify: BluetoothGattCharacteristic? = null
        private var onReceived: OnReceivedListener? = null
        private val pis by lazy { PipedInputStream(PIPE_SIZE) }
        private val pos by lazy { PipedOutputStream(pis) }
        private var receivedRunnable = Runnable {
            running = true
            val buf = ByteArray(PIPE_SIZE)
            while (running) {
                val read = pis.read(buf)
                if (running || read <= 0) {
                    break
                }
                onReceived?.onReceived(buf)
            }
        }
        private var running = false

        companion object {
            private const val PIPE_SIZE = 4096
        }

        fun setNotify(notify: BluetoothGattCharacteristic, pool: ThreadPoolExecutor) {
            this.notify = notify
            if (!running) {
                pool.execute(receivedRunnable)
            }
        }

        fun onReadListener(characteristic: BluetoothGattCharacteristic?) {
            characteristic ?: return
            val value = characteristic.value
            try {
                pos.write(value)
                pos.flush()
            } catch (e: IOException) {
                BluetoothHelper.logHelper.withEnable { "read io error: ${e.message}" }
            } catch (e: Exception) {
                BluetoothHelper.logHelper.withEnable { "read error: ${e.message}" }
            }
        }

        fun onReadListener(onReceived: OnReceivedListener) {
            this.onReceived = onReceived
        }

        override fun cancel() {
            running = false
            onReceived = null
            notify = null
        }

        override fun destroy() {
            cancel()
        }

    }

    internal class DataSender : Cancelable, Destroyable {
        private val bytesList = LinkedList<ByteArray>()
        private var gatt: BluetoothGatt? = null
        private var write: BluetoothGattCharacteristic? = null
        private var running = false
        private val lock = Object()
        private var sendSuccess = false
        private val sendRunnable = Runnable {
            running = true
            while (running) {

                if (bytesList.isEmpty()) {
                    lock.wait()
                }
                if (!running) {
                    break
                }
                sendSuccess = false
                if (gatt != null) {
                    write?.value = bytesList.first
                    gatt?.writeCharacteristic(write)

                    //wait sendSuccess or timeout
                }
            }
        }

        /**
         * 此方法没有检查bytes的大小是否超过最大值
         */
        fun send(bytes: ByteArray) {
            bytesList.add(bytes)
            lock.notify()
        }

        fun setGatt(gatt: BluetoothGatt) {
            this.gatt = gatt
        }

        fun setWrite(write: BluetoothGattCharacteristic, pool: ThreadPoolExecutor) {
            this.write = write
            if (!running) {
                pool.execute(sendRunnable)
            }
        }

        fun onSend(characteristic: BluetoothGattCharacteristic?) {
            characteristic ?: return
            sendSuccess = true
        }

        override fun cancel() {
            bytesList.clear()
        }

        override fun destroy() {
        }
    }

    interface CharacteristicChangedListener {

        fun onRead(characteristic: BluetoothGattCharacteristic?)

        fun onSend(characteristic: BluetoothGattCharacteristic?, status: Int)
    }
}