package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.lifecycle.LiveData
import com.munch.lib.extend.suspendCancellableCoroutine
import com.munch.lib.log.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/5/18 14:15.
 */
@SuppressLint("MissingPermission")
class BluetoothDev(val mac: String) : IBluetoothStop, Connector {

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

    private var connector: Connector = BleConnector(this, helper?.log ?: Logger("bluetooth"))

    override val curr: LiveData<ConnectState> = connector.curr

    override val currState: ConnectState
        get() = connector.currState

    val canConnect: Boolean
        get() = curr.value == ConnectState.Disconnected

    override fun addConnectHandler(handler: OnConnectHandler): BluetoothDev {
        connector.addConnectHandler(handler)
        return this
    }

    override fun removeConnectHandler(handler: OnConnectHandler): BluetoothDev {
        connector.removeConnectHandler(handler)
        return this
    }

    override fun connect(timeout: Long, connectListener: ConnectListener?): Boolean {
        //只能在非连接状态下才能连接，调用时应该使用队列，而不能并发
        helper?.devs?.forEach {
            if (!it.canConnect) {
                helper?.log?.log { "cannot connect now: ${it.mac}: ${currState}." }
                return false
            }
        }
        return connector.connect(timeout, connectListener)
    }

    /**
     * 找到mac地址的蓝牙对象
     *
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
        //todo 超时helper.remove(bondChange)
        return suspendCancellableCoroutine(timeout) {
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
        } ?: false
    }

    suspend fun removeBond(timeout: Long = 1000L): Boolean {
        if (!isValid) {
            return false
        }
        if (!isPair) {
            return true
        }
        val helper = helper ?: return false
        //todo 超时helper.remove(bondChange)
        return suspendCancellableCoroutine(timeout) {
            val bondChange = object : OnStateChangeListener {
                override fun onStateChange(state: StateNotify, mac: String?) {
                    if (state == StateNotify.BondNone) {
                        it.resume(!isPair)
                        helper.remove(this)
                    }
                }
            }
            helper.add(bondChange)

            val method = BluetoothDevice::class.java.getDeclaredMethod("removeBond")
            val result = method.invoke(dev) as? Boolean ?: false
            helper.log.log { "[$mac] remove bond(invoke): $result." }
            if (!result) {
                helper.remove(bondChange)
                it.resume(false)
            }
        } ?: false
    }

    override fun stop() = connector.stop()

    override fun toString() = "$name($mac)"
}