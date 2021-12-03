package com.munch.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import com.munch.lib.base.Destroyable
import com.munch.lib.log.Logger
import com.munch.lib.task.ThreadHandler

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

    private lateinit var context: Context
    private var initialized = false
    private lateinit var env: BluetoothEnv
    private val stateHelper = BluetoothStateHelper()
    private lateinit var th: ThreadHandler

    fun init(context: Context) {
        if (initialized) {
            return
        }
        initialized = true
        logHelper.withEnable { "BluetoothHelper init" }
        val c = context.applicationContext
        this.context = c
        env = BluetoothEnv(c)
        dispatchState()
        checkCurrentState()
        th = ThreadHandler("BLUETOOTH_WORK_THREAD")
    }

    val state: BluetoothStateHelper
        get() {
            checkInitialized()
            return stateHelper
        }
    val bluetoothEnv: BluetoothEnv
        get() {
            checkInitialized()
            return env
        }
    internal val handler: Handler
        get() {
            checkInitialized()
            return th
        }

    //todo 扫描回调
    fun scan(type: BluetoothType, parameter: ScanParameter? = null) {
        if (!stateHelper.canOp) {
            logHelper.withEnable { "cannot scan when ${state.currentState}" }
            return
        }
        Scanner(type).build(parameter).start()
    }

    private fun dispatchState() {
        env.setStateListener { _, state ->
            when (state) {
                BluetoothAdapter.STATE_ON -> checkCurrentState()
                BluetoothAdapter.STATE_OFF -> checkCurrentState()
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    //todo 取消扫描
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
    private fun checkInitialized() {
        require(initialized) { "must call init() first" }
    }

    override fun destroy() {
        env.destroy()
    }

}