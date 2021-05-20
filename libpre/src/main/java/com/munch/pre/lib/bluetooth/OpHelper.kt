package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.munch.pre.lib.base.Cancelable
import com.munch.pre.lib.base.Destroyable
import com.munch.pre.lib.extend.split
import com.munch.pre.lib.helper.ThreadPoolHelper
import com.munch.pre.lib.helper.file.closeQuietly
import java.io.IOException
import java.io.InterruptedIOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2021/4/27 17:29.
 */
class OpHelper : Cancelable, Destroyable {

    private val logHelp = BluetoothHelper.logHelper
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
    private val sender by lazy { DataSender() }
    private val reader by lazy { DataReader() }
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
        private val mtu =
            BluetoothHelper.INSTANCE.config.mtu.takeIf { it != -1 } ?: BtConfig.DEF_MTU
        private val pis by lazy { PipedInputStream(mtu) }
        private val pos by lazy { PipedOutputStream() }
        private var receivedRunnable = Runnable {
            running = true
            val buf = ByteArray(mtu)
            while (running) {
                val read: Int
                try {
                    read = pis.read(buf)
                    //当线程池被关闭时
                } catch (e: InterruptedIOException) {
                    break
                }
                if (!running || read <= 0) {
                    break
                }

                onReceived?.onReceived(buf.copyOfRange(0, read))
            }
        }
        private var running = false

        fun setNotify(notify: BluetoothGattCharacteristic, pool: ThreadPoolExecutor) {
            if (running) {
                throw IllegalStateException("must stop first")
            }
            this.notify = notify
            try {
                //因为没有判断绑定的方法
                pos.connect(pis)
            } catch (e: IOException) {
                //ignore
            }
            pool.execute(receivedRunnable)
        }

        fun onRead(characteristic: BluetoothGattCharacteristic?) {
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
            onReceived = null
            running = false
            notify = null
        }

        override fun destroy() {
            cancel()
            pos.closeQuietly()
            pis.closeQuietly()
        }

    }

    internal class DataSender : Cancelable, Destroyable {

        private var currentPack: SendPack? = null
        private val bytesList = LinkedList<SendPack>()
        private var gatt: BluetoothGatt? = null
        private var write: BluetoothGattCharacteristic? = null
        private var running = false
        private val lock = Object()
        private var writed = false
        private var received = false
        private var receivedBytes: ByteArray? = null
        private val sendRunnable = Runnable {
            running = true
            val mtu = BluetoothHelper.INSTANCE.config.mtu.takeIf { it != -1 } ?: BtConfig.DEF_MTU
            while (running) {

                if (bytesList.isEmpty()) {
                    synchronized(lock) {
                        try {
                            //当线程池先关闭时，此方法会报错
                            lock.wait()
                        } catch (e: Exception) {
                            return@Runnable
                        }
                    }
                }
                if (!running) {
                    break
                }
                if (bytesList.isEmpty()) {
                    continue
                }

                val pack = bytesList.removeFirst()
                val content = pack.bytes
                for (i in 1..pack.retryCount) {
                    if (content.size > mtu) {
                        content.split(mtu).forEach { write(it) }
                    } else {
                        write(content)
                    }
                    if (!pack.needReceived) {
                        pack.listener?.onReceived(byteArrayOf())
                        break
                    } else {
                        currentPack = pack
                        received = false
                        receivedBytes = null
                        val timeSleep = 3L
                        var count = pack.timeout / timeSleep
                        while (count > 0L) {
                            Thread.sleep(timeSleep)
                            count--
                            if (received && receivedBytes != null) {
                                pack.listener?.onReceived(receivedBytes!!)
                                break
                            }
                        }
                        if (received) {
                            break
                        }
                    }
                }
                if (!received) {
                    pack.listener?.onTimeout()
                }
            }
        }

        private fun write(byte: ByteArray) {
            if (gatt != null) {
                writed = false
                write?.value = byte
                gatt?.writeCharacteristic(write)

                var time = 100L
                do {
                    time--
                    TimeUnit.MILLISECONDS.sleep(1)
                } while (time > 0 && !writed)
            }
        }

        /**
         * 此方法没有检查bytes的大小是否超过最大值
         */
        fun send(pack: SendPack) {
            bytesList.add(pack)
            synchronized(lock) {
                lock.notify()
            }
        }

        fun setGatt(gatt: BluetoothGatt) {
            this.gatt = gatt
        }

        fun setWrite(write: BluetoothGattCharacteristic, pool: ThreadPoolExecutor) {
            if (running) {
                throw IllegalStateException("must stop first")
            }
            this.write = write
            pool.execute(sendRunnable)
        }

        fun onSend() {
            writed = true
        }

        override fun cancel() {
            bytesList.clear()
            running = false
            synchronized(lock) { lock.notify() }
        }

        override fun destroy() {
            cancel()
        }

        fun onReceived(bytes: ByteArray) {
            //因为发送是依次单项发送，因此可以直接当作单向接收
            //但是此次没有考虑线程问题
            received = currentPack?.listener?.onChecked(bytes) == true
            if (received) {
                receivedBytes = bytes
            }
        }
    }

    interface CharacteristicChangedListener {

        fun onRead(characteristic: BluetoothGattCharacteristic?)

        fun onSend(characteristic: BluetoothGattCharacteristic?, status: Int)
    }
}