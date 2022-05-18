package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/5/18 14:15.
 */
class BluetoothDev(private val mac: String) {

    companion object {

        fun from(result: ScanResult) = BluetoothDev(result.device)
    }

    private var helper: BluetoothHelper? = null

    constructor(dev: BluetoothDevice) : this(dev.address) {
        this.dev = dev
    }

    private var dev: BluetoothDevice? = null

    fun connect(): Boolean {
        return false
    }

    suspend fun find(): Boolean {
        if (dev != null) {
            return true
        }
        val helper = helper ?: return false
        val dev = helper.pairedDevs?.find { it.address == mac }
        if (dev != null) {
            this.dev = dev
            return true
        }
        helper.launch {
            suspendCancellableCoroutine<Boolean> {
                helper.scan(ScanTarget(), object : ScanListener {
                    override fun onDevScanned(dev: BluetoothDev) {
                        val device = dev.dev
                        this@BluetoothDev.dev = device
                        it.resume(device != null)
                    }
                })
            }
        }
        return false
    }
}