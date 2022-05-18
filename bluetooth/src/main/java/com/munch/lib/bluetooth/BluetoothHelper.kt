package com.munch.lib.bluetooth

import android.content.Context
import com.munch.lib.AppHelper
import com.munch.lib.Destroyable
import com.munch.lib.extend.SingletonHolder
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/5/18 14:15.
 */
class BluetoothHelper private constructor(
    context: Context,
    private val btWrapper: BluetoothWrapper = BluetoothWrapper(context),
    private val scanner: Scanner = BleScanner()
) : CoroutineScope, IBluetoothManager by btWrapper, IBluetoothState by btWrapper,
    Scanner by scanner, Destroyable {

    companion object : SingletonHolder<BluetoothHelper, Context>({ BluetoothHelper(it) }) {

        val instance = getInstance(AppHelper.app)
    }

    private val job = Job()

    fun isPair(mac: String) = pairedDevs?.any { it.address == mac } ?: false
    fun isGattConnect(mac: String) = connectGattDevs?.any { it.address == mac } ?: false

    override fun destroy() {
        job.cancel()
    }

    override val coroutineContext: CoroutineContext =
        Dispatchers.Default + CoroutineName("Bluetooth") + job
}