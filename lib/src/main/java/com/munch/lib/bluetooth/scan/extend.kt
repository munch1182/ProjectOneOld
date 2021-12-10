package com.munch.lib.bluetooth.scan

import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothType

/**
 * Create by munch1182 on 2021/12/6 09:24.
 */
typealias OnDeviceScanned = (dev: BluetoothDev) -> Unit
typealias OnBatchDeviceScanned = (devs: Array<BluetoothDev>) -> Unit

/**
 * ble需要android.Manifest.permission.ACCESS_FINE_LOCATION权限，否则结果不会回调
 */
@SuppressLint("InlinedApi")
@RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
inline fun BluetoothHelper.scan(type: BluetoothType, crossinline scanned: OnDeviceScanned) =
    scan(type, null, scanned)

/**
 * ble需要android.Manifest.permission.ACCESS_FINE_LOCATION权限，否则结果不会回调
 */
@SuppressLint("InlinedApi")
@RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
inline fun BluetoothHelper.scan(
    type: BluetoothType,
    parameter: ScanParameter?,
    crossinline scanned: OnDeviceScanned
) {
    scan(type, parameter, object : OnScannerListener {
        override fun onDeviceScanned(dev: BluetoothDev) {
            super.onDeviceScanned(dev)
            scanned.invoke(dev)
        }
    })
}

/**
 * ble需要android.Manifest.permission.ACCESS_FINE_LOCATION权限，否则结果不会回调
 */
@SuppressLint("InlinedApi")
@RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
inline fun BluetoothHelper.batchScan(
    type: BluetoothType,
    crossinline scanned: OnBatchDeviceScanned
) = batchScan(type, null, scanned)

/**
 * ble需要android.Manifest.permission.ACCESS_FINE_LOCATION权限，否则结果不会回调
 */
@SuppressLint("InlinedApi")
@RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
inline fun BluetoothHelper.batchScan(
    type: BluetoothType,
    parameter: ScanParameter?,
    crossinline scanned: OnBatchDeviceScanned
) {
    scan(type, parameter, object : OnScannerListener {
        override fun onBatchDeviceScanned(devs: Array<BluetoothDev>) {
            super.onBatchDeviceScanned(devs)
            scanned.invoke(devs)
        }
    })
}