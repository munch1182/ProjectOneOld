package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.collection.ArrayMap
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import cn.munch.lib.DBRecord
import com.munch.lib.AppHelper
import com.munch.lib.Destroyable
import com.munch.lib.extend.SingletonHolder
import com.munch.lib.log.InfoStyle
import com.munch.lib.log.Logger
import com.munch.lib.log.setOnLog
import com.munch.lib.log.setOnPrint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/5/18 14:15.
 */
class BluetoothHelper private constructor(
    context: Context,
    internal val log: Logger = Logger("bluetooth", infoStyle = InfoStyle.THREAD_ONLY),
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
    private val cache = ArrayMap<String, BluetoothDev>()
    internal val handler: Handler = dispatcher.handler

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

    init {
        scanner.helper = this
        btWrapper.setHandler(handler)
        //监听蓝牙关闭
        add(object : OnStateChangeListener {
            override fun onStateChange(state: StateNotify, mac: String?) {
                if (state == StateNotify.StateOff) {
                    stop()
                }
            }
        })
        log.setOnPrint { _, msg ->
            launch(Dispatchers.Default) { DBRecord.insert(msg) }
        }
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