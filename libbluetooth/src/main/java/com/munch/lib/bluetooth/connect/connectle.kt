package com.munch.lib.bluetooth.connect

import android.bluetooth.*
import androidx.annotation.WorkerThread
import com.munch.lib.android.extend.lazy
import com.munch.lib.android.extend.to
import com.munch.lib.bluetooth.helper.BluetoothHelperConfig
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import java.util.UUID
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
                "onConnectionStateChange: status: ${status.status()}, newState: ${newState.state()}, gatt: ${gatt.str()}."
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
                "onDescriptorRead: status: ${status.status()}, descriptor: ${descriptor.str()}."
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
                "onDescriptorWrite: status: ${status.status()}, descriptor: ${descriptor.str()}."
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
                "onCharacteristicRead: status: ${status.status()}, characteristic: ${characteristic.str()}."
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
                "onCharacteristicWrite: status: ${status.status()}, characteristic: ${characteristic.str()}."
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
                "onPhyRead: rxPhy: $txPhy, $rxPhy: $rxPhy, status: ${status.status()}, gatt: ${gatt.str()}."
            )
        }
    }

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        if (enableLog) {
            log(
                "onPhyUpdate: rxPhy: $txPhy, $rxPhy: $rxPhy, status: ${status.status()}, gatt: ${gatt.str()}."
            )
        }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        if (enableLog) {
            log(
                "onReadRemoteRssi: rssi: $rssi, status: ${status.status()}, gatt: ${gatt.str()}."
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
        BluetoothProfile.STATE_CONNECTED -> "CONNECT"
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
    IBluetoothHelperEnv by BluetoothHelperEnv {

    internal val mac = sysDev.address
    private var l: OnConnectStateChangeListener? = null
    private var _gatt: BluetoothGatt? = null
    private var curr = WaitResult()
    internal var writer: BluetoothGattCharacteristic? = null
    var currMtu: Int = 24
        private set

    companion object {
        private const val TAG = "gatt"
    }

    internal val callback by lazy {
        object : BluetoothGattBaseCallback(mac) {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (_gatt == null) _gatt = gatt
                l?.onConnectStateChange(status, newState)
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
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                curr.notify("onCharacteristicWrite", null, status == BluetoothGatt.GATT_SUCCESS)
            }
        }
    }

    internal fun setConnectStateListener(l: OnConnectStateChangeListener): BluetoothGattHelper {
        this.l = l
        return this
    }

    /**
     * 调用[BluetoothGatt.discoverServices]并同步返回结果
     *
     * @return 返回是否发现成功
     */
    @WorkerThread
    fun discoverServices(timeout: Long = BluetoothHelperConfig.builder.defaultTimeout): Boolean {
        _gatt?.discoverServices()?.takeIf { it } ?: return false
        if (enableLog) log("call discoverServices()", timeout)
        curr.wait("onServicesDiscovered", timeout)
        val success = curr.isGattSuccess
        if (enableLog) log("discoverServices: $success")
        return success
    }

    fun getService(uuid: UUID): BluetoothGattService? = _gatt?.getService(uuid)

    /**
     * 设置数据写入服务特征值
     *
     * 只有写入此值, 才能使用数据发送服务
     */
    fun setDataWriter(writer: BluetoothGattCharacteristic) {
        if (enableLog) log("set DataWriter")
        this.writer = writer
    }

    /**
     * 调用[BluetoothGatt.requestMtu]并同步返回结果
     *
     * @return 当前回调的mtu
     */
    @WorkerThread
    fun requestMtu(mtu: Int, timeout: Long = BluetoothHelperConfig.builder.defaultTimeout): Int? {
        _gatt?.requestMtu(mtu)?.takeIf { it } ?: return null
        if (enableLog) log("call requestMtu($mtu)", timeout)
        curr.wait("onMtuChanged", timeout)
        val currMtu: Int? = curr.any?.to()
        if (enableLog) log("onMtuChanged: ${currMtu == mtu}")
        this.currMtu = currMtu ?: 24
        return currMtu
    }

    /**
     * 调用[BluetoothGatt.readCharacteristic]并同步返回结果
     *
     * @return 回调返回的BluetoothGattCharacteristic对象
     */
    @WorkerThread
    fun readCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        timeout: Long = BluetoothHelperConfig.builder.defaultTimeout
    ): BluetoothGattCharacteristic? {
        _gatt?.readCharacteristic(characteristic)?.takeIf { it } ?: return null
        if (enableLog) log("call readCharacteristic(${characteristic.str()})", timeout)
        curr.wait("onCharacteristicChanged", timeout)
        val curr: BluetoothGattCharacteristic? = curr.any?.to()
        if (enableLog) log("readCharacteristic: ${curr?.str()}")
        return curr
    }

    /**
     * 调用[BluetoothGatt.writeCharacteristic]并同步返回结果
     *
     * @return 同步返回onCharacteristicWrite的characteristic对象, 如果超时则返回null
     */
    @WorkerThread
    fun writeCharacteristic(
        c: BluetoothGattCharacteristic,
        timeout: Long = 3 * 1000
    ): Boolean {
        _gatt?.writeCharacteristic(c)
        curr.wait("onCharacteristicWrite", timeout)
        val curr: Boolean? = curr.any?.to()
        log("writeCharacteristic: $curr")
        return curr ?: false
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