package com.munch.lib.bluetooth

import android.content.Context
import android.os.Handler
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.AppHelper
import com.munch.lib.Destroyable
import com.munch.lib.extend.SingletonHolder
import com.munch.lib.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/5/18 14:15.
 */
class BluetoothHelper private constructor(
    context: Context,
    internal val log: Logger = Logger("bluetooth"),
    private val btWrapper: BluetoothWrapper = BluetoothWrapper(context, log),
    private val scanner: BleScanner = BleScanner(log),
) : CoroutineScope,
    IBluetoothManager by btWrapper,
    IBluetoothState by btWrapper,
    Scanner by scanner,
    Destroyable {

    companion object : SingletonHolder<BluetoothHelper, Context>({ BluetoothHelper(it) }) {

        val instance = getInstance(AppHelper.app)
    }

    private val job = Job()
    private val dispatcher = BluetoothDispatcher()

    val handler: Handler
        get() = dispatcher.handler

    init {
        scanner.helper = this
        btWrapper.setHandler(handler)
    }


    fun isPair(mac: String) = pairedDevs?.any { it.address == mac } ?: false
    fun isGattConnect(mac: String) = connectGattDevs?.any { it.address == mac } ?: false

    fun setScanOnResume(owner: LifecycleOwner, listener: ScanListener) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                registerScanListener(listener)
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                unregisterScanListener(listener)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    override fun stop(): Boolean {
        if (isScanning.value == true) {
            return scanner.stop()
        }
        return true
    }

    override fun destroy() {
        job.cancel()
    }

    override val coroutineContext: CoroutineContext = dispatcher + job

}