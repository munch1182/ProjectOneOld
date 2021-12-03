package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcelable
import androidx.annotation.RequiresPermission
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
         * 如果[BluetoothDevice]中[BluetoothDevice.getType]为双模式，则默认使用[BluetoothType.BLE]，否则应自行构建
         * 如果[BluetoothDevice]中[BluetoothDevice.getType]未知，则会抛出异常
         */
        @SuppressLint("InlinedApi")
        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_CONNECT])
        fun from(dev: BluetoothDevice, rssi: Int = 0): BluetoothDev {
            val type = when (dev.type) {
                BluetoothDevice.DEVICE_TYPE_DUAL,
                BluetoothDevice.DEVICE_TYPE_LE -> BluetoothType.BLE
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> BluetoothType.CLASSIC
                else -> throw IllegalStateException("must set bluetoothType")
            }

            return BluetoothDev(dev.name, dev.address, rssi, type, dev)
        }

        @SuppressLint("InlinedApi")
        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_CONNECT])
        fun from(result: ScanResult) = from(result.device, result.rssi)

        @SuppressLint("InlinedApi")
        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_CONNECT])
        fun from(mac: String): BluetoothDev? {
            if (!BluetoothAdapter.checkBluetoothAddress(mac)) {
                return null
            }
            val adapter = BluetoothHelper.instance.bluetoothEnv.adapter
            return from(adapter?.getRemoteDevice(mac) ?: return null)
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
