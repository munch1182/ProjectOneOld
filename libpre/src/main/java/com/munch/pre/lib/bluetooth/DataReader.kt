package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import com.munch.pre.lib.helper.file.closeQuietly
import java.io.IOException
import java.io.InterruptedIOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.ThreadPoolExecutor

internal class DataReader : Manageable {
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