package com.munch.lib.bluetooth.connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import com.munch.lib.base.Destroyable
import com.munch.lib.base.split
import com.munch.lib.base.toHexStr
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.data.IData
import com.munch.lib.bluetooth.data.OnByteArrayReceived
import com.munch.lib.log.Logger
import com.munch.lib.task.ThreadHandler
import java.util.concurrent.CountDownLatch

/**
 * 1. callback的回调线程与此类执行方法的线程不能在同一线程，因为此类方法的执行会堵塞线程
 *
 * Create by munch1182 on 2021/12/8 09:38.
 */
open class GattWrapper(private val mac: String?, private val logger: Logger? = null) : IData,
    Destroyable {

    private val logHelper = BluetoothHelper.logHelper

    private var bleGatt: BluetoothGatt? = null
    private var c: CountDownLatch? = null
    private var tag: String = ""
    private var resultCode = 0
    private var mtu = 23
    private var rssi = -1
    private var descriptor: BluetoothGattDescriptor? = null
    private var phy: Pair<Int, Int>? = null
    val gatt: BluetoothGatt?
        get() = bleGatt

    private var writer: BluetoothGattCharacteristic? = null
    private var writeComplete = false
    private var received: OnByteArrayReceived? = null

    private val gattHandler by lazy { ThreadHandler("GATT_CALLBACK") }

    fun post(r: Runnable) {
        if (Thread.currentThread().id != gattHandler.thread.id) {
            gattHandler.post(r)
        } else {
            r.run()
        }
    }

    fun postDelay(r: Runnable, delayMillis: Long) {
        gattHandler.postDelayed(r, delayMillis)
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    @WorkerThread
    override fun send(byteArray: ByteArray) {
        post {
            val w = writer ?: return@post
            val mtu = if (mtu <= 0) 23 else mtu
            val allBytes = byteArray.split(mtu - 3)
            allBytes.forEach {
                w.value = it
                logHelper.withEnable { "${mac}: send: ${byteArray.toHexStr()}" }
                gatt?.writeCharacteristic(w)
                writeComplete = false
                //因为执行线程和回调线程都在同一线程，因此不需要加锁
                while (!writeComplete) {
                    Thread.sleep(1)
                }
            }
        }
    }

    override fun onReceived(received: OnByteArrayReceived) {
        this.received = received
    }

    override fun destroy() {
        gattHandler.quit()
        c?.countDown()
        c = null
    }

    /**
     * 设置写入特征
     *
     * 将使用此特征值来发送数据
     */
    fun setWriteCharacteristic(writer: BluetoothGattCharacteristic) {
        this.writer = writer
        logHelper.withEnable {
            "${mac}: setWriteCharacteristic: ${
                writer.toString().replace("android.bluetooth.BluetoothGattCharacteristic", "")
            }"
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun discoverServices(): Result<Void> {
        if (bleGatt?.discoverServices() == false) {
            return Result.fail()
        }
        waitResult("onServicesDiscovered")
        return Result(resultCode)
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun requestMtu(mtu: Int): Result<Int> {
        if (bleGatt?.requestMtu(mtu) == false) {
            return Result.fail()
        }
        waitResult("onMtuChanged")
        return Result(resultCode, mtu)
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun writeDescriptor(descriptor: BluetoothGattDescriptor): Result<BluetoothGattDescriptor> {
        if (bleGatt?.writeDescriptor(descriptor) == false) {
            return Result.fail()
        }
        waitResult("onDescriptorWrite")
        return Result(resultCode, descriptor)
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun readRemoteRssi(): Result<Int> {
        if (bleGatt?.readRemoteRssi() == false) {
            return Result.fail()
        }
        waitResult("onReadRemoteRssi")
        return Result(resultCode, rssi.takeIf { it != -1 })
    }

    /**
     * phy: txPhy,rxPhy
     *
     * @see android.bluetooth.BluetoothDevice.PHY_LE_1M
     * @see android.bluetooth.BluetoothDevice.PHY_LE_2M
     * @see android.bluetooth.BluetoothDevice.PHY_LE_CODED
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun readPhy(): Result<Pair<Int, Int>> {
        bleGatt?.readPhy()
        waitResult("onPhyRead")
        return Result(resultCode, phy)
    }

    private fun waitResult(tag: String) {
        require(c == null) { "wrong state." }
        c = CountDownLatch(1)
        this.tag = tag
        c?.await()
    }

    open fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {}

    val callback = object : BleGattCallback(mac, logger) {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            this@GattWrapper.bleGatt = gatt
            this@GattWrapper.onConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            updateAndNotify(status, "onServicesDiscovered")
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            this@GattWrapper.mtu = mtu
            updateAndNotify(status, "onMtuChanged")
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            this@GattWrapper.descriptor = descriptor
            updateAndNotify(status, "onDescriptorWrite")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            this@GattWrapper.rssi = rssi
            updateAndNotify(status, "onReadRemoteRssi")
        }

        override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
            this@GattWrapper.phy = txPhy to rxPhy
            updateAndNotify(status, "onPhyRead")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            /*super.onCharacteristicWrite(gatt, characteristic, status)*/
            writeComplete = true
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            /*super.onCharacteristicChanged(gatt, characteristic)*/
            val data = characteristic?.value ?: return
            received?.invoke(data)
        }
    }

    private fun updateAndNotify(status: Int, tag: String) {
        if (this.tag != tag) {
            return
        }
        resultCode = status
        c?.countDown()
        c = null
    }

    data class Result<T>(private val status: Int = BluetoothGatt.GATT_FAILURE, val obj: T? = null) {

        val isSuccess = status == BluetoothGatt.GATT_SUCCESS

        companion object {

            fun <T> fail() = Result<T>()
        }

    }
}