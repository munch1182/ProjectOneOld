@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.SparseArray
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.OnChangeListener
import com.munch.lib.extend.suspendCancellableCoroutine
import com.munch.lib.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.math.absoluteValue

private inline fun helperInstance() = BluetoothHelper

// 由BluetoothHelper提供Logger和CoroutineScope
interface BluetoothHelperFun : CoroutineScope {

    val log: Logger
        get() = BluetoothHelper.log

    override val coroutineContext: CoroutineContext
        get() = BluetoothHelper
}

// 由BluetoothHelper提供IBluetoothEnv
interface BluetoothHelperEnv {
    val env: IBluetoothEnv
        get() = BluetoothHelper
}

/**
 * 用于查找特定设备的过滤器
 */
class DeviceFindFilter(private val mac: String) : OnDeviceFilter {
    override fun isDeviceNeedFilter(dev: IBluetoothDev): Boolean {
        return mac == dev.mac
    }

    override fun toString() = "find $mac filter"
}

class DeviceScanFilter private constructor() : OnDeviceFilter {

    private var onlyOnce = false
    private var minRssi = 0
    private var keyName: String? = null
    private var keyMac: String? = null
    private var showNoName = true

    val map = SparseArray<BluetoothScanDev>()

    class Builder {
        private val filter = DeviceScanFilter()

        /**
         * 相同的设备只返回一次
         *
         * @param onlyOnce true过滤掉相同的设备, false不过滤
         */
        fun once(onlyOnce: Boolean): Builder {
            filter.onlyOnce = onlyOnce
            return this
        }

        /**
         * 过滤掉信号大于该值的设备, 值为绝对值
         *
         * 当[minRssi]等于0时不过滤
         */
        fun maxRssi(minRssi: Int): Builder {
            filter.minRssi = minRssi.absoluteValue
            return this
        }

        /**
         * 过滤掉设备中名称不包含参数name的设备
         *
         * 当[name]等于null时不过滤
         */
        fun name(name: String?): Builder {
            filter.keyName = name?.lowercase()
            return this
        }

        /**
         * 过滤掉设备中mac中不包含参数mac的设备
         *
         * 当[mac]等于null时不过滤
         */
        fun mac(mac: String?): Builder {
            filter.keyMac = mac?.lowercase()
            return this
        }

        /**
         * 是否过滤掉没有名称值的设备
         *
         * @param noName true过滤, false不过滤
         */
        fun noName(noName: Boolean = false): Builder {
            filter.showNoName = noName
            return this
        }

        fun build(): DeviceScanFilter {
            filter.reset()
            return filter
        }
    }

    private fun reset() {
        map.clear()
    }

    override fun isDeviceNeedFilter(dev: IBluetoothDev): Boolean {
        if (dev !is BluetoothScanDev) return true
        if (onlyOnce && map.indexOfKey(dev.mac.hashCode()) > 0) return true
        if (minRssi > 0 && dev.rssi.absoluteValue > minRssi) return true
        if (keyMac?.let { !dev.mac.lowercase().contains(it) } == true) return true
        val name = dev.device.name?.lowercase() ?: ""
        if (showNoName && name.isEmpty()) return true
        if (keyName?.let { !name.contains(it) } == true) return true
        map.put(dev.mac.hashCode(), dev)
        return false
    }

    override fun toString(): String {
        return "DeviceScanFilter(onlyOnce=$onlyOnce${if (minRssi > 0) ", minRssi=$minRssi" else ""}${keyName?.let { ", keyName=$keyName" } ?: ""}${keyMac?.let { ", keyMac=$keyMac" } ?: ""}, noName=$showNoName)"
    }


}

/**
 * 精简[OnDeviceScanListener]
 */
fun interface OnDeviceScannedListener {
    fun onDeviceScanned(dev: BluetoothScanDev)
}

fun ScanStateDispatcher.addDeviceScannedListener(listener: OnDeviceScannedListener) {
    addDeviceScanListener(object : OnDeviceScanListener {
        override fun onDeviceScanned(dev: BluetoothScanDev) {
            listener.onDeviceScanned(dev)
        }
    })
}

/**
 * addDeviceScanListener/removeDeviceScanListener -> observe, onResume/onPause
 */
fun ScanStateDispatcher.observeScan(owner: LifecycleOwner, listener: OnDeviceScanListener) {
    owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            addDeviceScanListener(listener)
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            removeDeviceScanListener(listener)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            owner.lifecycle.removeObserver(this)
        }
    })
}

/**
 * OnDeviceScannedListener -> observe, onResume/onPause
 */
fun ScanStateDispatcher.observeScanned(owner: LifecycleOwner, listener: OnDeviceScannedListener?) {
    observeScan(owner, object : OnDeviceScanListener {
        override fun onDeviceScanned(dev: BluetoothScanDev) {
            listener?.onDeviceScanned(dev)
        }
    })
}

/**
 * OnDeviceScanListener -> bool(isScanning), onResume/onPause
 *
 * @see observeScan
 */
fun Scanner.observeScan(owner: LifecycleOwner, onUpdate: OnChangeListener<Boolean>) {
    val listener = object : OnDeviceScanListener {
        private var isScanning = false
        override fun onDeviceScanned(dev: BluetoothScanDev) {
        }

        override fun onDeviceScanStart() {
            super.onDeviceScanStart()
            if (!isScanning) {
                isScanning = true
                onUpdate.invoke(isScanning)
            }
        }

        override fun onDeviceScanComplete() {
            super.onDeviceScanComplete()
            if (isScanning) {
                isScanning = false
                onUpdate.invoke(isScanning)
            }
        }
    }
    observeScan(owner, listener)
}

inline fun BluetoothDevice.toDev() = BluetoothDev(this)

/**
 * 通过扫描去寻找设备
 *
 * 不进行已连接设备判断, 因此已连接设备返回扫描不到返回null
 *
 * @return 返回已扫描到的蓝牙设备对象, 未找到则返回null
 */
suspend fun ScannerFun.find(
    mac: String,
    timeout: Long = BluetoothHelper.TIMEOUT_DEF
) = suspendCancellableCoroutine(helperInstance(), timeout) {
    startScan(DeviceFindFilter(mac), object : OnDeviceScanListener {
        override fun onDeviceScanned(dev: BluetoothScanDev) {
            stopScan()
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
        override fun onStateChange(state: StateNotify, mac: String?) {
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

/**
 * addStateChangeListener/removeStateChangeListener => observe, onCreate/onDestroy
 */
fun IBluetoothState.observeState(owner: LifecycleOwner, listener: OnStateChangeListener) {
    owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            this@observeState.addStateChangeListener(listener)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            this@observeState.removeStateChangeListener(listener)
            owner.lifecycle.removeObserver(this)
        }
    })
}