package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/5/18 14:15.
 */
@SuppressLint("MissingPermission")
class BluetoothDev(val mac: String) {

    companion object {

        fun from(result: ScanResult) = BluetoothDev(result.device).apply { rssi = result.rssi }
    }

    // TODO: 如何设置
    private var helper: BluetoothHelper? = BluetoothHelper.instance


    constructor(dev: BluetoothDevice) : this(dev.address) {
        this.dev = dev
    }

    private var dev: BluetoothDevice? = null

    val name: String?
        get() = dev?.name

    var rssi: Int = 0

    fun connect(): Boolean {
        return false
    }

    /**
     * 如果已被缓存，则返回true
     * 如果已被配对，则保存至缓存并返回true
     * 否在，将发起扫描查找该设备，如果找到则保存至缓存并返回true
     * 如果要在查找时间内即中止查找，则可以使用[BluetoothHelper.stop]，此时会立即返回false
     */
    suspend fun find(timeout: Long = ScanTarget.TIMEOUT): Boolean {
        if (dev != null) {
            return true
        }
        val helper = helper ?: return false
        val dev = helper.pairedDevs?.find { it.address == mac }
        if (dev != null) {
            this.dev = dev
            return true
        }
        return suspendCancellableCoroutine {
            val scanListener = object : SimpleScanListener {
                override fun onScanned(dev: BluetoothDev) {
                    val device = dev.dev
                    if (mac != dev.mac) {
                        it.resume(false)
                    } else {
                        this@BluetoothDev.dev = device
                        it.resume(device != null)
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    // 超时触发
                    if (this@BluetoothDev.dev == null) {
                        it.resume(false)
                    }
                }
            }
            helper.scan(ScanTarget().apply {
                filter = ScanFilter().apply { mac = this@BluetoothDev.mac }
                isFromUser = false
                this.timeout = timeout
            }, scanListener)
        }
    }

    override fun toString(): String {
        return "$name($mac)"
    }
}