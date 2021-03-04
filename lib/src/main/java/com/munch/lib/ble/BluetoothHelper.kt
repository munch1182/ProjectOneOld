package com.munch.lib.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import androidx.annotation.RequiresPermission
import com.munch.lib.helper.AddRemoveSetHelper
import com.munch.lib.RequiresPermission as Permission

/**
 * Create by munch1182 on 2021/3/2 16:55.
 */
@Permission(
    anyOf = [
        //蓝牙权限
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        //蓝牙扫描权限
        android.Manifest.permission.ACCESS_FINE_LOCATION]
)
@SuppressLint("MissingPermission")
class BluetoothHelper private constructor() {

    companion object {
        private val INSTANCE by lazy { BluetoothHelper() }

        fun getInstance() = INSTANCE

        const val NAME = "BLUETOOTH_HANDLER_THREAD"

        fun permissions() = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun getBtSetIntent() = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
    }

    internal lateinit var context: Context
    internal lateinit var instance: BtDeviceInstance
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var scannerHelper: BtScannerHelper
    private lateinit var connectHelper: BtConnectHelper
    internal lateinit var btAdapter: BluetoothAdapter

    fun init(context: Context) {
        this.context = context.applicationContext
        initWorkThread()
        instance = BtDeviceInstance(this.context)
        instance.getStateListeners().add { _, turning, available ->
            //蓝牙关闭中时
            if (turning && !available) {
                scannerHelper.stopScan()
            }
        }
        scannerHelper = BtScannerHelper(handler)
        btAdapter = instance.btAdapter
        connectHelper = BtConnectHelper()
    }

    private fun initWorkThread() {
        handlerThread = HandlerThread(NAME)
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    fun open(): Boolean {
        if (!instance.isEnable()) {
            return btAdapter.enable()
        }
        return true
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    fun close(): Boolean {
        if (instance.isEnable()) {
            return btAdapter.disable()
        }
        return true
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun startScan(
        type: BtType,
        scanFilter: MutableList<ScanFilter>? = null,
        timeout: Long = 0L,
        scanListener: BtScanListener? = null
    ) {
        scannerHelper.startScan(type, scanFilter, timeout, scanListener)
    }

    fun startClassicScan(
        scanFilter: MutableList<ScanFilter>? = null,
        timeout: Long = 0L,
        scanListener: BtScanListener? = null
    ) {
        startScan(BtType.Classic, scanFilter, timeout, scanListener)
    }

    fun startBleScan(
        scanFilter: MutableList<ScanFilter>? = null,
        timeout: Long = 0L,
        scanListener: BtScanListener? = null
    ) {
        startScan(BtType.Ble, scanFilter, timeout, scanListener)
    }

    fun stopScan() {
        scannerHelper.stopScan()
    }

    fun getScanListeners(): AddRemoveSetHelper<BtScanListener> = scannerHelper

    fun getStateListeners() = instance.getStateListeners()

    /**
     * 调用了此方法后需要重新调用[init]方法才能重新使用本类
     */
    fun destroy() {
        instance.release()
        scannerHelper.clear()
        handlerThread.quit()
    }
}