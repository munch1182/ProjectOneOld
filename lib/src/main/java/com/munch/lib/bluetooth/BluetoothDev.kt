package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Parcelable
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.connect.*
import com.munch.lib.task.ThreadHandler
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Create by munch1182 on 2021/12/3 16:23.
 */
@Parcelize
data class BluetoothDev(
    val name: String? = null,
    val mac: String? = null,
    var rssi: Int = 0,
    val type: @RawValue BluetoothType,
    val dev: BluetoothDevice? = null
) : Parcelable {

    companion object {

        /**
         * 从[BluetoothDevice]中构建[BluetoothDev]
         *
         * @param type 只有从系统已配对的设备才能为null，因为系统无缓存的设备dev.type为DEVICE_TYPE_UNKNOWN
         *
         * 如果[BluetoothDevice]中[BluetoothDevice.getType]为双模式，则默认使用[BluetoothType.BLE]，否则应自行构建
         * 如果[BluetoothDevice]中[BluetoothDevice.getType]未知，则会抛出异常
         */
        @SuppressLint("InlinedApi")
        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_CONNECT])
        fun from(
            dev: BluetoothDevice,
            rssi: Int = 0,
            type: @RawValue BluetoothType? = null
        ): BluetoothDev {
            val t = type ?: when (dev.type) {
                BluetoothDevice.DEVICE_TYPE_DUAL,
                BluetoothDevice.DEVICE_TYPE_LE -> BluetoothType.BLE
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> BluetoothType.CLASSIC
                else -> throw IllegalStateException("must set bluetoothType")
            }
            return BluetoothDev(dev.name, dev.address, rssi, t, dev)
        }

        fun from(result: ScanResult) = BluetoothDev(
            result.scanRecord?.deviceName,
            result.device.address,
            result.rssi,
            BluetoothType.BLE,
            result.device
        )

        @SuppressLint("InlinedApi")
        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_CONNECT])
        fun from(
            context: Context,
            mac: String,
            type: @RawValue BluetoothType
        ): BluetoothDev? {
            if (!BluetoothAdapter.checkBluetoothAddress(mac)) {
                return null
            }
            val adapter =
                (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager?)?.adapter
            return from(adapter?.getRemoteDevice(mac) ?: return null, 0, type)
        }
    }

    val rssiStr: String
        get() = "$rssi dBm"
    val isBle: Boolean
        get() = type.isBle
    val isClassic: Boolean
        get() = type.isClassic
    val bondState: Int
        @SuppressLint("InlinedApi") @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_CONNECT])
        get() = dev?.bondState ?: BluetoothDevice.BOND_NONE
    val isBond: Boolean
        @SuppressLint("InlinedApi") @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_CONNECT])
        get() = dev?.bondState == BluetoothDevice.BOND_BONDED
    val connectState: ConnectState
        get() = connector?.state ?: ConnectState.DISCONNECTED

    @SuppressLint("InlinedApi")
    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_CONNECT])
    fun createBond() = dev?.createBond() ?: false

    /**
     * 如果该设备已绑定，则移除该绑定；如果正在绑定，则尝试取消绑定；
     * 反射失败则返回null，否则返回boolean
     *
     * 注意：此方法只允许由app绑定的设备，不允许移除由其它设备添加的绑定
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_CONNECT])
    fun removeBond(): Boolean? {
        val d = dev ?: return null
        return when (d.bondState) {
            BluetoothDevice.BOND_NONE -> true
            BluetoothDevice.BOND_BONDING -> try {
                val cancelBond = BluetoothDevice::class.java.getDeclaredMethod("cancelBondProcess")
                cancelBond.isAccessible = true
                cancelBond.invoke(d) as? Boolean?
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            else -> try {
                val removeBond = BluetoothDevice::class.java.getDeclaredMethod("removeBond")
                removeBond.isAccessible = true
                removeBond.invoke(d) as? Boolean?
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun toString(): String {
        return "BluetoothDev(name=$name, mac=$mac, type=$type, rssiStr='$rssiStr')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BluetoothDev
        return mac == other.mac
    }

    override fun hashCode(): Int {
        return mac?.hashCode() ?: 0
    }

    //<editor-fold desc="CONNECT">
    @IgnoredOnParcel
    private var connector: Connector? = null

    fun setConnector(set: BleConnectSet? = null, handler: ThreadHandler): BluetoothDev {
        if (connector != null && connector?.state?.isDisconnected != true) {
            throw ConnectFail.DisallowConnect("cannot set Connector")
        }
        connector = Connector(this, set, handler)
        return this
    }

    fun setConnectorIfNeed(
        set: BleConnectSet? = null,
        handler: ThreadHandler
    ): BluetoothDev {
        connector ?: setConnector(set, handler)
        return this
    }

    /**
     * @see setConnector
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(listener: OnConnectListener) {
        connector?.setConnectListener(listener)?.connect()
    }

    /**
     * @see setConnector
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        connector?.disconnect()
    }

    fun setOnStateChangeListener(listener: OnConnectStateChange?) {
        connector?.setOnConnectStateChangeListener(listener)
    }
    //</editor-fold>
}

sealed class BluetoothType : Parcelable {

    val isBle: Boolean
        get() = this is BLE
    val isClassic: Boolean
        get() = this is CLASSIC

    @Parcelize
    object CLASSIC : BluetoothType() {

        override fun toString() = "CLASSIC"
    }

    @Parcelize
    object BLE : BluetoothType() {

        override fun toString() = "BLE"
    }
}
