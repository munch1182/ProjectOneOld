package com.munch.lib.bluetooth.connect

import android.bluetooth.*
import androidx.annotation.WorkerThread
import com.munch.lib.android.define.Notify
import com.munch.lib.android.extend.lazy
import com.munch.lib.android.extend.to
import com.munch.lib.android.helper.ARSHelper
import com.munch.lib.android.helper.IARSHelper
import com.munch.lib.bluetooth.data.BluetoothDataLogHelper.toSimpleLog
import com.munch.lib.bluetooth.data.BluetoothDataReceiver
import com.munch.lib.bluetooth.helper.BluetoothHelperConfig
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2022/10/28 15:32.
 */

open class BluetoothGattBaseCallback(
    private val mac: String,
) : BluetoothGattCallback(), IBluetoothHelperEnv by BluetoothHelperEnv {

    companion object {
        private const val TAG = "syst"
    }

    private fun log(content: String) {
        log.log("[$TAG]: [$mac]: $content")
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if (enableLog) {
            log(
                "onConnectionStateChange: status: ${status.status()}, newState: ${newState.state()}, gatt: ${gatt.str()}"
            )
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (enableLog) {
            log("onServicesDiscovered: status: ${status.status()}, gatt: ${gatt.str()}")
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        if (enableLog) {
            log("onMtuChanged: mtu: ${mtu}, status: ${status.status()}, gatt: ${gatt.str()}")
        }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
        if (enableLog) {
            log(
                "onDescriptorRead: status: ${status.status()}, descriptor: ${descriptor.str()}"
            )
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        if (enableLog) {
            log(
                "onDescriptorWrite: status: ${status.status()}, descriptor: ${descriptor.str()}"
            )
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        if (enableLog) {
            log(
                "onCharacteristicRead: status: ${status.status()}, characteristic: ${characteristic.str()}"
            )
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (enableLog) {
            log(
                "onCharacteristicWrite: status: ${status.status()}, characteristic: ${characteristic.str()}"
            )
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        if (enableLog) {
            log("onCharacteristicChanged: characteristic: ${characteristic.str()}")
        }
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
        if (enableLog) {
            log(
                "onPhyRead: rxPhy: $txPhy, $rxPhy: $rxPhy, status: ${status.status()}, gatt: ${gatt.str()}"
            )
        }
    }

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        if (enableLog) {
            log(
                "onPhyUpdate: rxPhy: $txPhy, $rxPhy: $rxPhy, status: ${status.status()}, gatt: ${gatt.str()}"
            )
        }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        if (enableLog) {
            log(
                "onReadRemoteRssi: rssi: $rssi, status: ${status.status()}, gatt: ${gatt.str()}"
            )
        }
    }

    override fun onServiceChanged(gatt: BluetoothGatt) {
        super.onServiceChanged(gatt)
        if (enableLog) {
            log("onServiceChanged: gatt: ${gatt.str()}")
        }
    }

    private fun Int.status() = when (this) {
        BluetoothGatt.GATT_READ_NOT_PERMITTED -> "GATT_READ_NOT_PERMITTED"
        BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "GATT_WRITE_NOT_PERMITTED"
        BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> "GATT_INSUFFICIENT_AUTHENTICATION"
        BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> "GATT_REQUEST_NOT_SUPPORTED"
        BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> "GATT_INSUFFICIENT_ENCRYPTION"
        BluetoothGatt.GATT_INVALID_OFFSET -> "GATT_INVALID_OFFSET"
        BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "GATT_INVALID_ATTRIBUTE_LENGTH"
        BluetoothGatt.GATT_CONNECTION_CONGESTED -> "GATT_CONNECTION_CONGESTED"
        BluetoothGatt.GATT_SUCCESS -> "SUCCESS"
        else -> toString()
    }

    private fun Int.state() = when (this) {
        BluetoothProfile.STATE_CONNECTED -> "CONNECTED"
        BluetoothProfile.STATE_DISCONNECTED -> "DISCONNECTED"
        BluetoothProfile.STATE_CONNECTING -> "CONNECTING"
        BluetoothProfile.STATE_DISCONNECTING -> "DISCONNECTING"
        else -> toString()
    }

}

internal fun BluetoothGatt?.str() =
    if (this == null) "null" else toString().replace("android.bluetooth.BluetoothGatt", "")

internal fun BluetoothGattDescriptor?.str() =
    if (this == null) "null" else toString().replace(
        "android.bluetooth.BluetoothGattDescriptor", ""
    )

internal fun BluetoothGattCharacteristic?.str() =
    if (this == null) "null" else toString().replace(
        "android.bluetooth.BluetoothGattCharacteristic", ""
    )

/**
 * 这些方法不能并发执行
 */
class BluetoothGattHelper(sysDev: BluetoothDevice) :
    IBluetoothConnectOperate,
    ARSHelper<BluetoothGattHelper.OnConnectStateChangeListener>(),
    IBluetoothHelperEnv by BluetoothHelperEnv {

    internal val mac = sysDev.address

    // 与ARSHelper不同, 此回调与dev绑定
    private var l: OnConnectStateChangeListener? = null
    private var _gatt: BluetoothGatt? = null
    private var curr = WaitResult()
    internal var writer: BluetoothGattCharacteristic? = null
    private val receiveLock = Mutex()
    private var receiver: BluetoothDataReceiver? = null
    private var setDataWriterCallback: Notify? = null
    var currMtu: Int = 24
        private set

    companion object {
        private const val TAG = "gatt"
    }

    val gatt: BluetoothGatt?
        get() = _gatt

    internal val callback by lazy {
        object : BluetoothGattBaseCallback(mac) {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (_gatt == null) _gatt = gatt
                l?.onConnectStateChange(status, newState)
                update { it.onConnectStateChange(status, newState) }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                curr.notify("onServicesDiscovered", status, null)
            }

            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
                curr.notify("onMtuChanged", status, mtu)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                curr.notify("onCharacteristicChanged", null, characteristic)
                launch {
                    if (BluetoothHelperConfig.config.enableLogReceiveOriginData) {
                        log("RECE <<<<< [${characteristic?.value?.toSimpleLog()}]")
                    }
                    receiver ?: return@launch
                    // 直接回调
                    receiveLock.withLock { characteristic?.value?.let { receiver?.onReceived(it) } }
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                curr.notify("onCharacteristicWrite", status, null)
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                super.onDescriptorWrite(gatt, descriptor, status)
                curr.notify("onDescriptorWrite", status, null)
            }
        }
    }

    // 连接不是这里发起的, 也不是这里关闭的
    internal fun close() {
        writer = null
        _gatt = null
    }

    internal fun setDataReceiver(receiver: BluetoothDataReceiver?): BluetoothGattHelper {
        this.receiver = receiver
        return this
    }

    internal fun setConnectStateListener(l: OnConnectStateChangeListener): BluetoothGattHelper {
        this.l = l
        return this
    }

    internal fun setDataWriterCallback(callback: Notify?): BluetoothGattHelper {
        setDataWriterCallback = callback
        return this
    }

    /**
     * 调用[BluetoothGatt.discoverServices]并同步返回结果
     *
     * @return 返回是否发现成功
     */
    @WorkerThread
    fun discoverServices(timeout: Long = BluetoothHelperConfig.config.defaultTimeout): Boolean {
        _gatt?.discoverServices()?.takeIf { it } ?: return false
        if (enableLog) log("call discoverServices()", timeout)
        curr.wait("onServicesDiscovered", timeout)
        val success = curr.isGattSuccess
        if (enableLog) log("discoverServices: $success")
        return success
    }

    fun getService(uuid: UUID): BluetoothGattService? = _gatt?.getService(uuid)
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic?,
        enable: Boolean
    ): Boolean? {
        return _gatt?.setCharacteristicNotification(characteristic ?: return false, enable)
    }

    /**
     * 调用[android.bluetooth.BluetoothGatt.writeDescriptor]并等待
     * [android.bluetooth.BluetoothGattCallback.onDescriptorWrite]返回后返回其status是否成功
     * 如果超时则返回false
     */
    fun writeDescriptor(
        descriptor: BluetoothGattDescriptor,
        timeout: Long = BluetoothHelperConfig.config.defaultTimeout
    ): Boolean {
        _gatt?.writeDescriptor(descriptor)?.takeIf { it } ?: return false
        if (enableLog) log("call writeDescriptor()", timeout)
        curr.wait("onDescriptorWrite", timeout)
        val isSuccess = curr.isGattSuccess
        if (enableLog) log("writeDescriptor: $isSuccess")
        return isSuccess
    }

    /**
     * 设置数据写入服务特征值
     *
     * 只有写入此值, 才能使用数据发送服务
     */
    fun setDataWriter(writer: BluetoothGattCharacteristic) {
        if (enableLog) log("SETUP DataWriter")
        this.writer = writer
        setDataWriterCallback?.invoke()
    }

    /**
     * 调用[BluetoothGatt.requestMtu]并等待
     * [android.bluetooth.BluetoothGattCallback.onMtuChanged]返回结果后返回当前mtu值是否和[mtu]一致
     *
     * @return 当前回调的mtu, 如果超时则返回false
     */
    @WorkerThread
    fun requestMtu(mtu: Int, timeout: Long = BluetoothHelperConfig.config.defaultTimeout): Int? {
        _gatt?.requestMtu(mtu)?.takeIf { it } ?: return null
        if (enableLog) log("call requestMtu($mtu)", timeout)
        curr.wait("onMtuChanged", timeout)
        val currMtu: Int? = curr.any?.to()
        if (enableLog) log("requestMtu: ${currMtu == mtu}")
        this.currMtu = currMtu ?: 24
        return currMtu
    }

    /**
     * 调用[BluetoothGatt.readCharacteristic]并等待
     * [android.bluetooth.BluetoothGattCallback.onCharacteristicRead]返回结果后返回的[BluetoothGattCharacteristic]对象
     *
     * @return 回调返回的BluetoothGattCharacteristic对象
     */
    @WorkerThread
    fun readCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        timeout: Long = BluetoothHelperConfig.config.defaultTimeout
    ): BluetoothGattCharacteristic? {
        _gatt?.readCharacteristic(characteristic)?.takeIf { it } ?: return null
        if (enableLog) log("call readCharacteristic(${characteristic.str()})", timeout)
        curr.wait("onCharacteristicChanged", timeout)
        val curr: BluetoothGattCharacteristic? = curr.any?.to()
        if (enableLog) log("readCharacteristic: ${curr?.str()}")
        return curr
    }

    /**
     * 调用[BluetoothGatt.writeCharacteristic]并等待
     * [android.bluetooth.BluetoothGattCallback.onCharacteristicWrite]返回结果后返回其status是否成功
     *
     * @return 同步返回onCharacteristicWrite的characteristic对象, 如果超时则返回false
     */
    @WorkerThread
    fun writeCharacteristic(
        c: BluetoothGattCharacteristic,
        timeout: Long = 3 * 1000
    ): Boolean {
        _gatt?.writeCharacteristic(c) ?: return false
        curr.wait("onCharacteristicWrite", timeout)
        val curr = curr.isGattSuccess
        log("writeCharacteristic: $curr")
        return curr
    }

    private fun log(content: String, timeout: Long) {
        log("$content with timeout $timeout ms")
    }

    private fun log(content: String) {
        log.log("[$TAG]: [$mac]: $content")
    }

    private class WaitResult {

        /**
         *唤醒时传递的status值
         */
        private var status: Int? = null

        /**
         * 唤醒时传递的值
         */
        private var currAny: Any? = null

        private var currTag: String? = null
        private var currCD: CountDownLatch? = null
            get() = synchronized(this) { field }
            set(value) = synchronized(this) { field = value }

        val isGattSuccess: Boolean
            get() = status?.let { it == BluetoothGatt.GATT_SUCCESS } ?: false

        val any: Any?
            get() = currAny


        /**
         * 阻塞当前线程, 等待[notify]唤醒
         */
        fun wait(tag: String, timeout: Long) {
            if (currCD != null) {
                throw IllegalArgumentException("?")
            }
            status = null
            currAny = null
            currTag = tag
            currCD = CountDownLatch(1)
            currCD?.await(timeout, TimeUnit.MILLISECONDS)
        }

        /**
         * 唤醒[wait], [wait]即可获取当前结果
         */
        fun notify(tag: String, status: Int?, any: Any?) {
            if (currTag != tag) return // 其它方法回调触发, 无视
            if (currCD == null) {
                throw IllegalArgumentException("?")
            }
            this.status = status
            this.currAny = any
            currCD?.countDown()
            currCD = null
            currTag = null
        }
    }

    fun interface OnConnectStateChangeListener {
        fun onConnectStateChange(status: Int, newState: Int)
    }
}

/**
 * 用于统一处理状态变更
 */
internal interface IBluetoothGattNotify :
    IARSHelper<BluetoothGattHelper.OnConnectStateChangeListener> {

    val gattHelper: BluetoothGattHelper
}