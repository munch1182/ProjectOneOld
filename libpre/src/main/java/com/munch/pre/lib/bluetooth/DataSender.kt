package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.munch.pre.lib.extend.split
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class DataSender : Manageable {

    private var currentPack: SendPack? = null
    private val bytesList = LinkedList<SendPack>()
    private var gatt: BluetoothGatt? = null
    private var write: BluetoothGattCharacteristic? = null
    private var running = false
    private val lock = Object()
    private var hadWrite = false
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
            hadWrite = false
            write?.value = byte
            gatt?.writeCharacteristic(write)

            var time = 100L
            do {
                time--
                TimeUnit.MILLISECONDS.sleep(1)
            } while (time > 0 && !hadWrite)
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
        hadWrite = true
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