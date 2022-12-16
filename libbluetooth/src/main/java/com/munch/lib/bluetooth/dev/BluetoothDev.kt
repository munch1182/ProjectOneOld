package com.munch.lib.bluetooth.dev

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import com.munch.lib.android.helper.ARSParameterHelper
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.connect.*
import com.munch.lib.bluetooth.data.*
import com.munch.lib.bluetooth.helper.BluetoothHelperConfig
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Create by munch1182 on 2022/9/29 15:47.
 */
abstract class BluetoothDev internal constructor(
    override val mac: String,
    val type: BluetoothType = BluetoothType.UNKNOWN
) : IBluetoothDev, IBluetoothConnector, IBluetoothDataHandler, IBluetoothDataDispatcher {

    internal constructor(dev: BluetoothDevice) : this(dev.address, BluetoothType.from(dev))

    override fun toString() = mac

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BluetoothDev
        if (mac != other.mac) return false
        return true
    }

    override fun hashCode(): Int {
        return mac.hashCode()
    }

    fun toScanned(): BluetoothScannedDev? = if (this is BluetoothScannedDev) this else null
}

abstract class BluetoothScannedDev(val dev: BluetoothDevice) : BluetoothDev(dev) {

    val name: String?
        get() = dev.name
    open val rssi: Int?
        get() = null

    val rssiStr: String?
        get() = rssi?.let { "${it}dBm" }

    private val ars = ARSParameterHelper<OnBluetoothConnectStateListener>()
    private val lock = Mutex()

    // 初始状态只能是Disconnected, 因为本库没有进行连接
    protected open var _connectState: BluetoothConnectState = BluetoothConnectState.Disconnected
        get() = runBlocking { lock.withLock { field } }
        set(value) {
            runBlocking {
                lock.withLock {
                    if (field != value) {
                        val last = field
                        field = value
                        ars.update2 { it.onConnectState(value, last, this@BluetoothScannedDev) }
                        if (value.isDisconnecting || value.isDisconnected) { // 避免跳过了Disconnecting的情形
                            close()
                        }
                        log("Connect STATE update: $last -> $value")
                    }
                }
            }
        }
    override val connectState: BluetoothConnectState
        get() = _connectState

    override fun addConnectListener(l: OnBluetoothConnectStateListener) {
        ars.add(l)
    }

    override fun removeConnectListener(l: OnBluetoothConnectStateListener) {
        ars.remove(l)
    }

    override suspend fun connect(
        timeout: Long,
        config: BluetoothConnector.Config?
    ): BluetoothConnectResult {
        if (connectState == BluetoothConnectState.Connected) {
            return BluetoothConnectResult.Success
        }
        if (connectState == BluetoothConnectState.Connecting) {
            return BluetoothConnectFailReason.ConnectedButConnect.toReason()
        }
        _connectState = BluetoothConnectState.Connecting
        var connectResult = connectImp(timeout, config)
        if (connectResult.isSuccess) {
            val judge = config?.judge ?: BluetoothHelperConfig.config.connectConfig.judge
            if (judge != null) {
                log("start JUDGE")
                connectResult = judge.onJudge(operate)
                log("JUDGE result: $connectResult")
            }
        }
        if (connectResult.isSuccess) { // 连接成功
            dataHandler.active()
            _connectState = BluetoothConnectState.Connected
        } else { // 连接失败, 则需要尝试断开
            disconnect()
        }
        return connectResult
    }


    override suspend fun disconnect(removeBond: Boolean): Boolean {
        if (_connectState.isConnected || _connectState.isConnecting) {
            _connectState = BluetoothConnectState.Disconnecting
        }
        log("start DISCONNECT")
        val result = disconnectImp(removeBond)
        log("DISCONNECT: $result")
        _connectState = BluetoothConnectState.Disconnected
        return result
    }

    protected open fun close() {
        dataHandler.inactive()
    }

    override suspend fun send(pack: ByteArray): Boolean {
        if (_connectState.isConnected) {
            return dataHandler.send(pack)
        }
        return false
    }

    override fun setReceiver(receiver: BluetoothDataReceiver?) = dataHandler.setReceiver(receiver)

    override fun cancelSend() = dataHandler.cancelSend()

    override fun addReceiver(receiver: BluetoothDataReceiver) {
        dataHandler.addReceiver(receiver)
    }

    override fun removeReceiver(receiver: BluetoothDataReceiver) {
        dataHandler.removeReceiver(receiver)
    }

    abstract val operate: IBluetoothConnectOperate
    protected abstract val dataHandler: IBluetoothDataManger
    protected abstract suspend fun connectImp(
        timeout: Long,
        config: BluetoothConnector.Config?
    ): BluetoothConnectResult

    protected abstract suspend fun disconnectImp(removeBond: Boolean): Boolean

    companion object {
        private const val TAG = "conn"
    }

    protected fun log(content: String) {
        if (BluetoothHelperConfig.config.enableLog) {
            BluetoothHelper.log.log("[$TAG]: [$dev]: $content")
        }
    }
}

internal class BluetoothLeDevice(
    dev: BluetoothDevice,
    private val scan: ScanResult?,
) : BluetoothScannedDev(dev), BluetoothLeDev {

    constructor(scan: ScanResult) : this(scan.device, scan)

    private val gattHelper by lazy { BluetoothGattHelper(dev) }
    private val connectFun by lazy { BluetoothLeConnectFun(dev, gattHelper) }
    override val dataHandler: IBluetoothDataManger by lazy { BluetoothLeDataHelper(gattHelper) }
    override val operate: IBluetoothConnectOperate by lazy { gattHelper }

    override fun close() {
        super.close()
        gattHelper.close()
    }

    private val connectStateListener by lazy {
        BluetoothGattHelper.OnConnectStateChangeListener { _, newState ->
            if (!_connectState.isConnecting) {
                _connectState = BluetoothConnectState.from(newState)
            }
        }
    }

    override val rssi: Int?
        get() = scan?.rssi

    override val rawRecord: ByteArray?
        get() = scan?.scanRecord?.bytes

    override suspend fun connect(
        timeout: Long,
        config: BluetoothConnector.Config?
    ): BluetoothConnectResult {
        gattHelper.setConnectStateListener(connectStateListener)
        return super.connect(timeout, config)
    }

    override suspend fun disconnect(removeBond: Boolean): Boolean {
        if (!_connectState.isDisconnected) {
            _connectState = BluetoothConnectState.Disconnecting
        }
        val disconnect = super.disconnect(removeBond)
        _connectState = BluetoothConnectState.Disconnected
        return disconnect
    }

    override suspend fun connectImp(
        timeout: Long,
        config: BluetoothConnector.Config?
    ) = connectFun.connect(timeout, config)

    override suspend fun disconnectImp(removeBond: Boolean) = connectFun.disconnect(removeBond)
}

class BluetoothClassicDevice(
    dev: BluetoothDevice,
    override val rssi: Int?
) : BluetoothScannedDev(dev) {
    override val dataHandler: IBluetoothDataManger
        get() = TODO("Not yet implemented")
    override val operate: IBluetoothConnectOperate
        get() = TODO("Not yet implemented")

    override suspend fun connectImp(
        timeout: Long,
        config: BluetoothConnector.Config?
    ): BluetoothConnectResult {
        TODO("Not yet implemented")
    }

    override suspend fun disconnectImp(removeBond: Boolean): Boolean {
        TODO("Not yet implemented")
    }

}