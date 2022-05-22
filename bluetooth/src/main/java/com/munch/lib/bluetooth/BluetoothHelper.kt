package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.collection.ArrayMap
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.AppHelper
import com.munch.lib.Destroyable
import com.munch.lib.extend.SingletonHolder
import com.munch.lib.log.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/5/18 14:15.
 */
class BluetoothHelper private constructor(
    context: Context,
    private val btWrapper: BluetoothWrapper = BluetoothWrapper(context),
    private val scanner: BleScanner = BleScanner(btWrapper),
) : CoroutineScope,
    IBluetoothManager by btWrapper,
    IBluetoothState by btWrapper,
    Scanner by scanner,
    Destroyable {

    companion object : SingletonHolder<BluetoothHelper, Context>({ BluetoothHelper(it) }) {

        val instance = getInstance(AppHelper.app)

        internal val log: Logger = Logger("bluetooth", infoStyle = LogStyle.THREAD)
    }

    private val job = SupervisorJob()
    private val dispatcher = BluetoothDispatcher()
    private val cache = ArrayMap<String, BluetoothDev>()
    private val handler: Handler = dispatcher.handler

    /**
     * 获取当前已经程序已经连接的设备
     */
    val devs: List<BluetoothDev>
        get() = cache.values.toList()

    /**
     * gatt已经连接的设备
     */
    override val connectGattDevs: List<BluetoothDevice>?
        get() = btWrapper.connectGattDevs
    private val onStateOff = object : OnStateChangeListener {
        override fun onStateChange(state: StateNotify, mac: String?) {
            if (state == StateNotify.StateOff) {
                stop()
            }
        }
    }

    init {
        btWrapper.setHandler(handler)
        scanner.setHandler(handler)
        //监听蓝牙关闭
        add(onStateOff)
    }

    fun get(mac: String): BluetoothDev? = cache[mac]

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
        cache.values.forEach { it.stop() }
        return true
    }

    override fun destroy() {
        job.cancel()
        remove(onStateOff)
    }

    /**
     * 当设备连接时，需要缓存设备对象
     */
    internal fun cacheDev(dev: BluetoothDev) {
        cache[dev.mac] = dev
    }

    /**
     * 当设备断开连接时，需要清除该缓存
     */
    internal fun clearDev(mac: String) {
        cache.remove(mac)
    }

    override val coroutineContext: CoroutineContext = dispatcher + job
}