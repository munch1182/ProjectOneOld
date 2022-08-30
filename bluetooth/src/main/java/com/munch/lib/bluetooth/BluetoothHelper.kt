package com.munch.lib.bluetooth

import android.bluetooth.le.ScanSettings
import android.content.Context
import com.munch.lib.AppHelper
import com.munch.lib.extend.HandlerDispatcher
import com.munch.lib.log.LogStyle
import com.munch.lib.log.Logger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * 注意: BluetoothEnv/Scanner 等类初始化时, BluetoothHelper还未建立实例, 因此不能在初始时使用BluetoothHelper参数
 */
object BluetoothHelper :
    HandlerDispatcher("BluetoothHandler"),
    CoroutineScope,
    Scanner by DispatchScanner(BluetoothEnv),
    IBluetoothManager by BluetoothEnv,
    IBluetoothState by BluetoothEnv {

    const val TIMEOUT_DEF = 30 * 1000L
    internal val log = Logger("bluetooth", style = LogStyle.THREAD)

    fun init(app: Context = AppHelper): BluetoothHelper {
        BluetoothEnv.init(app)
        return this
    }

    fun setBleScanSetting(scan: ScanSettings? = null): BluetoothHelper {
        BluetoothEnv.setBleScanSetting(scan)
        return this
    }

    private val job = SupervisorJob()
    private val name = CoroutineName("BluetoothHelper")

    override val coroutineContext: CoroutineContext
        get() = job + name + this

    /**
     * 销毁后无法在应用内重建
     */
    override fun destroy() {
        super.destroy()
        job.cancel()
    }
}

