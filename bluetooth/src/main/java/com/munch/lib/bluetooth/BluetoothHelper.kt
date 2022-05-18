package com.munch.lib.bluetooth

import android.content.Context
import android.os.Handler
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
    private val log: Logger = Logger("bluetooth"),
    private val btWrapper: BluetoothWrapper = BluetoothWrapper(context),
    private val scanner: BleScanner = BleScanner(log),
) : CoroutineScope,
    IBluetoothManager by btWrapper,
    IBluetoothState by btWrapper,
    Scanner by scanner,
    Destroyable {

    companion object : SingletonHolder<BluetoothHelper, Context>({ BluetoothHelper(it) }) {

        val instance = getInstance(AppHelper.app)
    }

    init {
        scanner.helper = this
    }

    private val job = Job()
    private val dispatcher = BluetoothDispatcher()

    val handler: Handler
        get() = dispatcher.handler

    fun isPair(mac: String) = pairedDevs?.any { it.address == mac } ?: false
    fun isGattConnect(mac: String) = connectGattDevs?.any { it.address == mac } ?: false

    override fun destroy() {
        job.cancel()
    }

    override val coroutineContext: CoroutineContext = dispatcher + job

}