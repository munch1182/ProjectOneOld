package com.munch.lib.bluetooth.helper

import com.munch.lib.android.extend.catch
import com.munch.lib.bluetooth.dev.BluetoothDev
import com.munch.lib.bluetooth.dev.BluetoothType
import com.munch.lib.bluetooth.scan.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicInteger

/**
 * 同时实现[IBluetoothOnceScanner]和[IBluetoothDevScanner], 因为其基本逻辑一致, 实际上也可以同时使用
 *
 * 主要实现过滤和分发
 *
 * Create by munch1182 on 2022/10/27 9:53.
 */
internal class BluetoothImpScanner(
    private val config: BluetoothHelperScanner.Builder,
    private val id: Int = 0 //#0即默认扫描器
) : BluetoothStateScanner(),
    IBluetoothOnceScanner,
    IBluetoothDevScanner {

    /**
     * 根据[config]实际调用的[IBluetoothDevScanner]
     */
    private lateinit var scanner: IBluetoothDevScanner

    /**
     * 外部设置的listener
     */
    private var setOnceListener: OnBluetoothOwnerDevScannedListener? = null

    private var channelJob: Job? = null
    private var channel: Channel<BluetoothDev>? = null

    /**
     * 给scanner实际传递的回调, 通过回调过滤并分发到此类的回调中
     */
    private val finListener = object : OnBluetoothDevScanListener {
        override fun onBluetoothDevScanned(dev: BluetoothDev) {
            launch { catch { channel?.send(dev) } }
        }

        override fun onScanStart() {
            super.onScanStart()
            val l = setOnceListener
            if (l is OnBluetoothDevScanListener) l.onScanStart()
            update { if (it is OnBluetoothDevScanListener) it.onScanStart() }
            val f = config.filter
            if (f is OnBluetoothDevLifecycleFilter) f.onStart()
        }

        override fun onScanStop() {
            super.onScanStop()
            val l = setOnceListener
            if (l is OnBluetoothDevScanListener) l.onScanStop()
            update { if (it is OnBluetoothDevScanListener) it.onScanStop() }
            val f = config.filter
            if (f is OnBluetoothDevLifecycleFilter) f.onStop()

            stopScan()
        }
    }

    override fun startScan(timeout: Long) {
        if (!isEnable) {
            log("start Scan but bluetooth is OFF, ignore.")
            return
        }
        if (isScanning) return

        val l = setOnceListener

        scanning = true

        scanner = when (config.type) {
            BluetoothType.LE -> BluetoothLeDevScanner
            BluetoothType.CLASSIC -> BluetoothClassicScanner
            else -> throw IllegalArgumentException()
        }
        scanner.addScanListener(finListener)

        log("start ${config.type} scan with timeout: $timeout ms")

        // 内部实现不能进行超时设置
        scanner.startScan(3 * 60 * 1000L) // 暂定时间, 如果设置错误, 该时间后也会自动关闭

        val devChannel = Channel<BluetoothDev>()
        channel = devChannel
        val job = SupervisorJob()
        channelJob = job

        launch(Dispatchers.IO + job) {
            val f = config.filter
            val time = config.delayTime
            withTimeoutOrNull(timeout) {
                for (dev in devChannel) {
                    if (f?.isNeedBeFilter(dev) == true) {
                        continue
                    }
                    if (time > 0) delay(time)
                    update { it.onBluetoothDevScanned(dev) }
                    l?.onBluetoothDevScanned(this@BluetoothImpScanner, dev)
                }
            }
            log("timeout to call stop scan.")
            // timeout
            stopScan()
        }
    }

    override fun stopScan() {
        if (!scanning) return

        channelJob?.cancel()
        channel?.close()
        channelJob = null
        channel = null


        log("stop ${config.type} scan.")

        scanning = false // 先更改此scanning的值, 避免stopScan重复调用
        if (setOnceListener is OnBluetoothDevScanLifecycleListener) {
            (setOnceListener as OnBluetoothDevScanLifecycleListener).onScanStop()
        }
        setOnceListener = null // setOnceListener会被自动移除,

        scanner.stopScan() // 实际调用关闭扫描

    }

    override fun setScanListener(l: OnBluetoothOwnerDevScannedListener): IBluetoothScanner {
        this.setOnceListener = l
        return this
    }

    override fun addScanListener(l: OnBluetoothDevScannedListener) {
        add(l)
    }

    override fun removeScanListener(l: OnBluetoothDevScannedListener) {
        remove(l)
    }

    override fun log(content: String) {
        if (BluetoothHelperConfig.builder.enableLog) {
            log.log("SCANNER #$id: $content")
        }
    }
}

/**
 * 管理默认扫描器和新建的扫描器
 */
internal class BluetoothHelperImpScanner : IBluetoothHelperScanner {

    private val scannerID = AtomicInteger(1)

    private val defaultBuilder = BluetoothHelperScanner.Builder()
        .filter(BluetoothDevFirstFilter(), BluetoothDevNoNameFilter())
        .type(BluetoothType.LE)

    private val defaultScanner = BluetoothImpScanner(defaultBuilder) //#0即默认扫描器

    override fun newScanner(builder: BluetoothHelperScanner.Builder): IBluetoothOnceScanner {
        return BluetoothImpScanner(builder, scannerID.getAndIncrement())
    }

    override fun configDefaultScan(config: BluetoothHelperScanner.Builder.() -> Unit) {
        config.invoke(defaultBuilder)
    }

    override val isScanning: Boolean
        get() = defaultScanner.isScanning

    override fun startScan(timeout: Long) {
        defaultScanner.startScan(timeout)
    }

    override fun stopScan() {
        defaultScanner.stopScan()
    }

    override fun addScanListener(l: OnBluetoothDevScannedListener) {
        defaultScanner.add(l)
    }

    override fun removeScanListener(l: OnBluetoothDevScannedListener) {
        defaultScanner.remove(l)
    }
}