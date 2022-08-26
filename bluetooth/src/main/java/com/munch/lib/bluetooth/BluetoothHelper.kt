package com.munch.lib.bluetooth

import android.bluetooth.le.ScanSettings
import android.content.Context
import com.munch.lib.AppHelper
import com.munch.lib.extend.SingletonHolder
import com.munch.lib.log.LogStyle
import com.munch.lib.log.Logger
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class BluetoothHelper(
    context: Context,
    env: BluetoothEnv = BluetoothEnv.init(context.applicationContext),
    scanner: Scanner = ScannerImp(env)
) : CoroutineDispatcher(),
    CoroutineScope,
    Scanner by scanner,
    IBluetoothManager by env,
    IBluetoothState by env {

    companion object : SingletonHolder<BluetoothHelper, Context>({ BluetoothHelper(it) }) {

        const val TIMEOUT_DEF = 30 * 1000L

        val instance: BluetoothHelper
            get() = BluetoothHelper.getInstance(AppHelper.app)
    }

    internal val log = Logger("bluetooth", style = LogStyle.THREAD)

    fun setBleScanSetting(scan: ScanSettings? = null): BluetoothHelper {
        BluetoothEnv.setBleScanSetting(scan)
        return this
    }

    private val job = SupervisorJob()
    private val jobAndName = job + CoroutineName("BluetoothHelper")

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + jobAndName

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        launch(context, block = { block.run() })
    }

}