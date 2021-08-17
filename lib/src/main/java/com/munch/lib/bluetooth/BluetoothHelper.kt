package com.munch.lib.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import com.munch.lib.log.Logger

/**
 * Create by munch1182 on 2021/8/17 9:48.
 */
class BluetoothHelper private constructor() {

    companion object {

        val instance by lazy { BluetoothHelper() }

        fun permissions() = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun getBluetoothIntent() = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)

        /**
         * 检查[mac]地址是否有效，即是否是合法的格式
         *
         * 注意：并不能检查地址是否存在
         */
        fun checkMac(mac: String) = BluetoothAdapter.checkBluetoothAddress(mac)

        private val logSystem = Logger().apply {
            tag = "bluetooth-system"
            noStack = true
        }
        private val logHelper = Logger().apply {
            tag = "bluetooth-helper"
            noStack = true
        }

    }
}