package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import com.munch.lib.log.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/5/18 14:15.
 */
@SuppressLint("MissingPermission")
class BluetoothDev(val mac: String) : IBluetoothStop {

    companion object {

        fun from(result: ScanResult) = BluetoothDev(result.device).apply { rssi = result.rssi }
    }

    // TODO: 如何设置
    private var helper: BluetoothHelper? = BluetoothHelper.instance

    constructor(dev: BluetoothDevice) : this(dev.address) {
        this.dev = dev
    }

    var dev: BluetoothDevice? = null
        private set

    val name: String?
        get() = dev?.name

    var rssi: Int = 0

    val isPair: Boolean
        get() = helper?.isPair(mac) ?: false

    val isValid: Boolean
        get() {
            val b = dev != null
            if (!b) {
                helper?.log?.log { "dev $mac is invalid." }
            }
            return b
        }

    private var connector = BleConnector(this, helper?.log ?: Logger("bluetooth"))

    override fun stop(): Boolean {
        return helper?.stop() ?: false
    }

    fun connect(
        timeout: Long = 15 * 1000L,
        connectHandler: OnConnectHandler? = null,
        connectListener: ConnectListener? = null
    ): Boolean {
        connector.helper = helper
        connector.setConnectHandler(connectHandler)
        return connector.connect(timeout, connectListener)
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
            helper.scan(ScanTarget {
                ScanFilter { mac = this@BluetoothDev.mac }
                isFromUser = false
                this.timeout = timeout
            }, scanListener)
        }
    }

    /**
     * 如果不是[isValid]，则会直接返回false，此时可能需要先调用[find]
     * 如果已经在配对列表，则会之间返回true
     * 否则会调用[BluetoothDevice.createBond]方法来发起配对操作
     * 如果在[timeout]ms时间内无响应，则会返回失败
     * 否则，返回是否配对的结果
     *
     * 配对服务需要用户手动确认
     */
    suspend fun createBond(timeout: Long = 20000L): Boolean {
        if (!isValid) {
            return false
        }
        if (isPair) {
            return true
        }
        val helper = helper ?: return false
        return withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine {
                val bondChange = object : OnStateChangeListener {
                    override fun onStateChange(state: StateNotify, mac: String?) {
                        if (state == StateNotify.BondNone || state == StateNotify.Bonded) {
                            it.resume(isPair)
                            helper.remove(this)
                        }

                    }
                }
                helper.add(bondChange)

                val result = dev?.createBond() ?: false
                helper.log.log { "[$mac] create bond: $result." }
                if (!result) {
                    helper.remove(bondChange)
                    it.resume(false)
                }
            }
        } ?: false
    }

    override fun toString() = "$name($mac)"
}