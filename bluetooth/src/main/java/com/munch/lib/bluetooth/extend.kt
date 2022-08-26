@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import com.munch.lib.extend.suspendCancellableCoroutine
import kotlin.coroutines.resume

private inline fun helperInstance() = BluetoothHelper.instance


class DeviceFindFilter(private val mac: String) : OnDeviceFilter {
    override fun isDeviceNeedFilter(dev: IBluetoothDev): Boolean {
        return mac == dev.mac
    }
}

fun interface OnDeviceScannedListener {
    fun onDeviceScanned(dev: BluetoothScanDev)
}

fun Scanner.addDeviceScannedListener(listener: OnDeviceScannedListener) {
    addDeviceScanListener(object : OnDeviceScanListener {
        override fun onDeviceScanned(dev: BluetoothScanDev) {
            listener.onDeviceScanned(dev)
        }
    })
}

inline fun BluetoothDevice.toDev() = BluetoothDev(this)

/**
 * 通过扫描去寻找设备
 *
 * 不进行已连接设备判断, 因此已连接设备返回扫描不到返回null
 *
 * @return 返回已扫描到的蓝牙设备对象, 未找到则返回null
 */
suspend fun Scanner.find(
    mac: String,
    timeout: Long = BluetoothHelper.TIMEOUT_DEF
) = suspendCancellableCoroutine(helperInstance(), timeout) {
    startScan(DeviceFindFilter(mac), object : OnDeviceScanListener {
        override fun onDeviceScanned(dev: BluetoothScanDev) {
            if (it.isActive) it.resume(dev)
        }

        override fun onDeviceScanComplete() {
            if (it.isActive) it.resume(null)
        }
    })
}

/**
 * 进行配对, 并返回配对结果
 *
 * 不进行配对判断, 因此如果已配对设备调用此方法可能会因为dev.createBond()而返回失败
 *
 * @return 如果配对成功, 则返回true
 */
suspend fun IBluetoothState.createBond(
    dev: BluetoothDevice,
    timeout: Long = BluetoothHelper.TIMEOUT_DEF
) = suspendCancellableCoroutine(helperInstance(), timeout) {
    val listener = object : OnStateChangeListener {
        override fun onStateChange(mac: String?, state: StateNotify) {
            if (dev.address == mac) {
                if (state != StateNotify.Bonding) {
                    removeStateChangeListener(this)
                }
                if (state == StateNotify.BondNone) {
                    it.resume(false)
                } else if (state == StateNotify.Bonded) {
                    it.resume(true)
                }
            }
        }
    }
    addStateChangeListener(listener)
    val create = dev.createBond()
    helperInstance().log.log { "[${dev.address}] CREATE BOND: $create." }
    if (!create) {
        removeStateChangeListener(listener)
        it.resume(false)
    }
} ?: false
