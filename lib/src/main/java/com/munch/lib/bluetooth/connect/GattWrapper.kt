package com.munch.lib.bluetooth.connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import androidx.annotation.RequiresPermission
import com.munch.lib.base.Destroyable
import com.munch.lib.log.Logger
import com.munch.lib.task.ThreadHandler
import java.util.concurrent.CountDownLatch

/**
 * 1. callback的回调线程与此类执行方法的线程不能在同一线程，因为此类方法的执行会堵塞线程
 *
 * Create by munch1182 on 2021/12/8 09:38.
 */
open class GattWrapper(private val mac: String?, private val logger: Logger) : Destroyable {

    private var bleGatt: BluetoothGatt? = null
    private var c: CountDownLatch? = null
    private var tag: String = ""
    private var resultCode = 0
    private var mtu = -1
    private var rssi = -1
    private var descriptor: BluetoothGattDescriptor? = null
    private var phy: Pair<Int, Int>? = null
    val gatt: BluetoothGatt?
        get() = bleGatt
    private val gattHandler by lazy { ThreadHandler("GATT_CALLBACK") }

    fun post(r: Runnable) {
        gattHandler.post(r)
    }

    fun postDelay(r: Runnable, delayMillis: Long) {
        gattHandler.postDelayed(r, delayMillis)
    }

    override fun destroy() {
        gattHandler.quit()
        c?.countDown()
        c = null
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
        return Result(resultCode, mtu.takeIf { it != -1 })
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