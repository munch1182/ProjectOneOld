package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.base.Destroyable
import com.munch.lib.bluetooth.connect.BleConnectSet
import com.munch.lib.bluetooth.connect.ConnectFail
import com.munch.lib.bluetooth.connect.OnConnectListener
import com.munch.lib.bluetooth.scan.OnScannerListener
import com.munch.lib.bluetooth.scan.ScanParameter
import com.munch.lib.bluetooth.scan.Scanner
import com.munch.lib.log.Logger
import com.munch.lib.task.ThreadHandler
import java.lang.ref.WeakReference

/**
 * Create by munch1182 on 2021/12/3 14:42.
 */
class BluetoothHelper private constructor() : Destroyable {

    companion object {

        /**
         * 需要自行调用[init]
         */
        val instance by lazy { BluetoothHelper() }

        fun getInstance(context: Context): BluetoothHelper {
            return instance.apply { init(context) }
        }

        /**
         * 该字符串是否是合法的蓝牙地址格式
         */
        fun String?.isBluetoothMac() =
            this?.let { BluetoothAdapter.checkBluetoothAddress(it) } ?: false

        internal val logSystem = Logger("bluetooth-system", true)
        internal val logHelper = Logger("bluetooth-helper", true)
    }

    private var initialized = false
    private lateinit var env: BluetoothEnv
    private val stateHelper = BluetoothStateHelper()
    private lateinit var th: ThreadHandler
    private var weakReference: WeakReference<Context>? = null

    fun init(context: Context) {
        if (initialized) {
            return
        }
        initialized = true
        logHelper.withEnable { "BluetoothHelper init" }
        val c = context.applicationContext
        weakReference = WeakReference(c)
        env = BluetoothEnv(c)
        dispatchState()
        checkCurrentState()
        th = ThreadHandler("BLUETOOTH_WORK_THREAD")
    }

    val state: BluetoothStateHelper
        get() = checkInitialized().stateHelper
    val bluetoothEnv: BluetoothEnv
        get() = checkInitialized().env
    private val handler: ThreadHandler
        get() = checkInitialized().th
    private val context: Context
        get() = weakReference?.get() ?: throw IllegalStateException("cannot get context")

    //<editor-fold desc="SCAN">
    private val scanner by lazy { Scanner(context, handler) }

    private var scanListener: OnScannerListener? = null

    /**
     * 使用回调的方法监听，避免scanner直接调用此类的参数
     */
    private val scannerCallBack = object : OnScannerListener {
        override fun onScanStart() {
            super.onScanStart()
            stateHelper.updateSCANNINGState()
            scanListener?.onScanStart()
        }

        override fun onDeviceScanned(dev: BluetoothDev) {
            super.onDeviceScanned(dev)
            scanListener?.onDeviceScanned(dev)
        }

        override fun onBatchDeviceScanned(devs: Array<BluetoothDev>) {
            super.onBatchDeviceScanned(devs)
            scanListener?.onBatchDeviceScanned(devs)
        }

        override fun onScanComplete() {
            super.onScanComplete()
            stateHelper.updateIDLEState()
            scanListener?.onScanComplete()
        }

        override fun onScanFail() {
            super.onScanFail()
            stateHelper.updateIDLEState()
            scanListener?.onScanFail()
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    fun scan(type: BluetoothType, listener: OnScannerListener? = null) {
        scan(type, null, listener)
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    fun scan(
        type: BluetoothType,
        parameter: ScanParameter?,
        listener: OnScannerListener? = null
    ) {
        if (!stateHelper.canOp) {
            logHelper.withEnable { "cannot scan when ${state.currentState}" }
            return
        }
        listener?.let { scanListener = it }
        scanner.type(type).build(parameter).setScanListener(scannerCallBack).start()
    }

    /**
     * OnScannerListener只会被设置一个，即[scan]参数中的OnScannerListener会和此参数的相互替换
     */
    fun setScanListener(listener: OnScannerListener? = null): BluetoothHelper {
        scanListener = listener
        return this
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    fun scanBle(
        parameter: ScanParameter? = null,
        listener: OnScannerListener? = null
    ) = scan(BluetoothType.BLE, parameter, listener)

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    fun scanClassic(
        parameter: ScanParameter? = null,
        listener: OnScannerListener? = null
    ) = scan(BluetoothType.CLASSIC, parameter, listener)

    /**
     * OnScannerListener只会被设置一个，即[scan]参数中的OnScannerListener会和此参数的相互替换
     */
    fun setScanListener(owner: LifecycleOwner, listener: OnScannerListener? = null) {
        setScanListener(listener)
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                setScanListener(null)
            }
        })
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        scanner.stop()
    }
    //</editor-fold>

    //<editor-fold desc="CONNECT">
    private val connectCallBack = object : OnConnectListener {
        override fun onConnectStart(dev: BluetoothDev) {
            super.onConnectStart(dev)
            stateHelper.updateCONNECTINGState()
        }

        override fun onConnected(dev: BluetoothDev) {
            stateHelper.updateIDLEState()
        }

        override fun onConnectFail(dev: BluetoothDev, fail: ConnectFail) {
            super.onConnectFail(dev, fail)
            stateHelper.updateIDLEState()
        }
    }
    private var connectListener: OnConnectListener? = null
    private var bleConnectSet: BleConnectSet? = null

    /**
     * 设置统一的连接设置
     */
    fun setConnectSet(set: BleConnectSet): BluetoothHelper {
        this.bleConnectSet = set
        return this
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(dev: BluetoothDev, listener: OnConnectListener? = null) {
        if (!stateHelper.canOp) {
            connectCallBack.onConnectFail(
                dev,
                ConnectFail.DisallowConnected("current ble state:${stateHelper.currentState}")
            )
            return
        }
        dev.setConnectorIfNeed(bleConnectSet, handler).connect(connectCallBack)
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect(dev: BluetoothDev) {
        dev.disconnect()
    }
    //</editor-fold>

    @SuppressLint("MissingPermission")
    private fun dispatchState() {
        env.setStateListener { _, state ->
            when (state) {
                BluetoothAdapter.STATE_ON -> checkCurrentState()
                BluetoothAdapter.STATE_OFF -> checkCurrentState()
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    try {
                        scanner.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    //todo 去断开所有连接
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                    //nothing
                }
            }
        }
        env.setBondStateListener { dev, state ->
            //todo 更新某个连接的状态
        }
    }

    private fun checkCurrentState() {
        checkInitialized()
        //蓝牙不支持
        if (!env.isBtSupport) {
            stateHelper.updateUNKNOWNState()
            //蓝牙已关闭
        } else if (!env.isEnable) {
            stateHelper.updateIDLEState()
            stateHelper.updateCLOSEState()
            //蓝牙已开启
        } else {
            //如果是初始，则更新
            if (stateHelper.isUNKNOWN) {
                stateHelper.updateIDLEState()
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun checkInitialized(): BluetoothHelper {
        require(initialized) { "must call init() first" }
        return this
    }

    override fun destroy() {
        env.destroy()
    }
}