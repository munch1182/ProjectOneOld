package com.munch.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcelable
import androidx.annotation.RequiresPermission
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Create by munch1182 on 2021/8/24 13:41.
 */
sealed class BluetoothType : Parcelable {

    /**
     * 经典蓝牙
     */
    @Parcelize
    object Classic : BluetoothType() {

        override fun toString(): String {
            return "classic"
        }
    }

    /**
     * 低功耗蓝牙
     */
    @Parcelize
    object Ble : BluetoothType() {

        override fun toString(): String {
            return "ble"
        }
    }

    val isBle: Boolean
        get() = this == Ble
    val isClassic: Boolean
        get() = this == Classic
}

@Parcelize
data class BluetoothDev(
    val name: String? = null,
    val mac: String,
    var rssi: Int = 0,
    val type: @RawValue BluetoothType,
    val dev: BluetoothDevice,
    //只有通过扫描获取的设备才有该值
    var scanResult: ScanResult? = null
) : Parcelable {

    companion object {

        @RequiresPermission(android.Manifest.permission.BLUETOOTH)
        fun from(result: ScanResult) =
            from(result.device, result.rssi).apply { scanResult = result }

        @RequiresPermission(android.Manifest.permission.BLUETOOTH)
        fun from(
            device: BluetoothDevice,
            rssi: Int = 0
        ): BluetoothDev {
            val type = if (BluetoothDevice.DEVICE_TYPE_CLASSIC == device.type) {
                BluetoothType.Classic
            } else {
                BluetoothType.Ble
            }
            return BluetoothDev(device.name, device.address, rssi, type, device)
        }

        @RequiresPermission(android.Manifest.permission.BLUETOOTH)
        fun from(mac: String, type: BluetoothType = BluetoothType.Ble): BluetoothDev? {
            if (!BluetoothAdapter.checkBluetoothAddress(mac)) {
                return null
            }
            val device = BluetoothHelper.instance.set.adapter?.getRemoteDevice(mac) ?: return null
            return BluetoothDev(device.name, device.address, 0, type, device)
        }
    }

    val rssiStr: String
        get() = "$rssi dBm"
    val isBle: Boolean
        get() = type == BluetoothType.Ble
    val isClassic: Boolean
        get() = type == BluetoothType.Classic

    /**
     * 是否已被本应用连接
     * @see isConnectedByGatt
     */
    val isConnectedByHelper: Boolean
        get() = mac == BluetoothHelper.instance.connectedDev?.mac
    val isConnecting: Boolean
        get() = BluetoothHelper.instance.state.isConnecting && mac == BluetoothHelper.instance.operationDev?.mac
    val bondState: Int
        @RequiresPermission(android.Manifest.permission.BLUETOOTH)
        get() = dev.bondState
    val isBond: Boolean
        @RequiresPermission(android.Manifest.permission.BLUETOOTH)
        get() = dev.bondState == BluetoothDevice.BOND_BONDED

    /**
     * 通过[BluetoothHelper]进行连接
     */
    fun connect() = BluetoothHelper.instance.connect(this)

    /**
     * 通过[BluetoothHelper]断开连接
     */
    fun disconnect() = BluetoothHelper.instance.disconnect()

    /**
     * 用于获取该蓝牙设备是否处于gatt连接状态
     * 因其实现，不建议批量使用
     *
     * @see isConnectedBySystem
     * @see com.munch.lib.bluetooth.BluetoothInstance.getConnectedDevice
     */
    @RequiresPermission(android.Manifest.permission.BLUETOOTH)
    fun isConnectedByGatt() = BluetoothHelper.instance.set.isConnectedByGatt(this)

    /**
     * 通过反射获取当前蓝牙设备的连接状态，包括gatt连接
     *
     * @see isConnectedByGatt
     */
    fun isConnectedBySystem(): Boolean? {
        return try {
            val isConnect = BluetoothDevice::class.java.getDeclaredMethod("isConnected")
            isConnect.isAccessible = true
            isConnect.invoke(dev) as? Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 如果该设备已绑定，则移除该绑定；如果正在绑定，则尝试取消绑定；
     * 反射失败则返回null，否则返回boolean
     *
     * 注意：此方法只允许由app绑定的设备，不允许移除由其它设备添加的绑定
     */
    @RequiresPermission(android.Manifest.permission.BLUETOOTH)
    fun removeBond(): Boolean? {
        return when (dev.bondState) {
            BluetoothDevice.BOND_NONE -> true
            BluetoothDevice.BOND_BONDING -> try {
                val cancelBond = BluetoothDevice::class.java.getDeclaredMethod("cancelBondProcess")
                cancelBond.isAccessible = true
                cancelBond.invoke(dev) as? Boolean?
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            else -> try {
                val removeBond = BluetoothDevice::class.java.getDeclaredMethod("removeBond")
                removeBond.isAccessible = true
                removeBond.invoke(dev) as? Boolean?
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_ADMIN)
    fun createBond() = dev.createBond()

    override fun toString(): String {
        return "BtDevice(name=$name, mac='$mac', rssi=$rssi, type=$type)"
    }

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

}