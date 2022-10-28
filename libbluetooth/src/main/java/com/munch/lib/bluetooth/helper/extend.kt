package com.munch.lib.bluetooth.helper

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanSettings
import com.munch.lib.android.extend.UpdateJob
import com.munch.lib.android.helper.ILifecycle
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.dev.BluetoothScanDev
import com.munch.lib.bluetooth.env.BluetoothNotify
import com.munch.lib.bluetooth.env.IBluetoothState
import com.munch.lib.bluetooth.env.OnBluetoothStateNotifyListener
import com.munch.lib.bluetooth.scan.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/10/27 11:26.
 */

//<editor-fold desc="scan">
/**
 * 在[life]内, 当[BluetoothHelper.isScanning]更改时, 回调[isScanning]
 */
fun IBluetoothHelperScanner.watchScan(life: ILifecycle, isScanning: (Boolean) -> Unit) {
    watchDevScan(life, object : OnBluetoothDevScanListener {
        override fun onBluetoothDevScanned(dev: BluetoothScanDev) {
        }

        override fun onScanStart() {
            super.onScanStart()
            isScanning.invoke(true)
        }

        override fun onScanStop() {
            super.onScanStop()
            isScanning.invoke(false)
        }
    })
}

fun IBluetoothHelperScanner.watchDevScan(
    life: ILifecycle,
    l: OnBluetoothDevScannedListener
) {
    life.onActive { addScanListener(l) }
    life.onInactive { removeScanListener(l) }
}

/**
 * 在[life]内, 当[BluetoothHelper.addScanListener]回调时, 将回调数据改为所有发现的设备的集合并回调到[devsScanned]
 */
fun IBluetoothHelperScanner.watchDevsScan(
    life: ILifecycle,
    devsScanned: OnBluetoothDevsScannedListener
) = watchDevScan(life, devsScanned.toDevScanned())

fun IBluetoothHelperScanner.newScanBuilder() = BluetoothHelperScanner.Builder()

/**
 * 通过扫描来寻找[mac]的设备, 如果在[timeout]时间内找到, 则返回该设备对象, 否则返回null
 *
 * 此方法会先从已配对设置中查找, 如果找到, 则直接返回配对的设备, 返回的设备也带有dev对象, 但是没有广播数据
 * 此方法是通过一个新建的扫描器, 不会影响默认或者其它的扫描器
 *
 * 此方法会阻塞
 */
suspend fun IBluetoothHelperScanner.find(
    mac: String,
    timeout: Long = BluetoothHelperConfig.builder.defaultTimeout
): BluetoothDevice? = suspendCancellableCoroutine {
    val device = BluetoothHelper.pairedDevs?.find { dev -> dev.address == mac }
    if (device != null) {
        it.resume(device)
    } else {
        newScanBuilder()
            .filter(BluetoothDevFindFilter(mac), BluetoothDevFirstFilter())
            .newScanner()
            .setScanListener(object : OnBluetoothOwnerDevScanListener {
                override fun onBluetoothDevScanned(
                    scanner: IBluetoothScanner,
                    dev: BluetoothScanDev
                ) {
                    if (it.isActive) it.resume(dev.dev)
                    scanner.stopScan()
                }

                override fun onScanStop() {
                    super.onScanStop()
                    if (it.isActive) it.resume(null)
                }
            }).startScan(timeout)
    }
}

fun IBluetoothHelperScanner.stopThenStartScan(timeout: Long = 30 * 1000L) {
    stopScan()
    startScan(timeout)
}

fun OnBluetoothDevsScannedListener.toDevScanned(): OnBluetoothDevScanListener =
    BluetoothDevsScannedListenerWrapper(this)

fun interface OnBluetoothDevsScannedListener {
    /**
     * 当扫描到一个设备的回调, 但返回的是扫描到的所有设备
     */
    fun onDevScanned(dev: MutableList<BluetoothScanDev>)
}

/**
 * 将[OnBluetoothDevsScannedListener]实现并转为[OnBluetoothDevScanListener], 同时避免结果高频率触发
 */
class BluetoothDevsScannedListenerWrapper(private val devs: OnBluetoothDevsScannedListener) :
    OnBluetoothDevScanListener, IBluetoothHelperEnv by BluetoothHelperEnv {

    private val map = linkedMapOf<String, BluetoothScanDev>()

    private var job = UpdateJob()

    /**
     * 上一次扫描到的设备的时间
     */
    private var lastScannedTime = 0L

    /**
     * 上一个回调的时间
     */
    private var lastNotifyTime = 0L

    override fun onScanStart() {
        loop2Notify()
    }

    private fun loop2Notify() {
        if (!job.isCancel()) {
            return
        }
        launch(Dispatchers.Default + job.cancelAndNew()) {
            while (true) {
                job.curr ?: break
                delay(200L)
                if (lastNotifyTime < lastScannedTime) {
                    devs.onDevScanned(map.values.toMutableList())
                }
                lastNotifyTime = System.currentTimeMillis()
                if (lastNotifyTime - lastScannedTime > 2000L) { // 如果2s没有新的扫描
                    break // 则不再循环, 去等待onDevScanned唤醒
                }
                delay(250L)
            }
            close()
        }
    }

    private fun close() {
        job.cancel()
    }

    override fun onScanStop() {
        close()
        map.clear()
        lastNotifyTime = 0L
        lastScannedTime = 0L
    }

    override fun onBluetoothDevScanned(dev: BluetoothScanDev) {
        map[dev.mac] = dev
        lastScannedTime = System.currentTimeMillis()
        if (job.isCancel()) { // 如果已经超时停止, 则由此处唤醒
            loop2Notify()
        }
    }
}

fun IBluetoothHelperScanner.setLeScan(set: ScanSettings): IBluetoothHelperScanner {
    BluetoothLeDevScanner.setScanSetting(set)
    return this
}
//</editor-fold>

//<editor-fold desc="notify">
typealias BluetoothOnOff = (onOff: Boolean) -> Unit

fun BluetoothOnOff.onOff2Notify(): OnBluetoothStateNotifyListener {
    return OnBluetoothStateNotifyListener { state, _ ->
        if (state == BluetoothNotify.StateOff) {
            invoke(false)
        } else if (state == BluetoothNotify.StateOn) {
            invoke(true)
        }
    }
}

/**
 * 监听蓝牙开关
 */
fun IBluetoothState.watchOnOff(life: ILifecycle, update: BluetoothOnOff) {
    val l = update.onOff2Notify()
    life.onActive { addStateChangeListener(l) }
    life.onInactive { removeStateChangeListener(l) }
}

typealias BluetoothBondResult = (isSuccess: Boolean) -> Unit

fun BluetoothBondResult.bondResult2Notify(mac: String): OnBluetoothStateNotifyListener {
    return OnBluetoothStateNotifyListener { state, _ ->
        when (state) {
            is BluetoothNotify.Bonded -> if (state.mac == mac) invoke(true)
            is BluetoothNotify.BondFail -> if (state.mac == mac) invoke(false)
            else -> {}
        }
    }
}

fun IBluetoothState.watchBond(life: ILifecycle, mac: String, bondResult: BluetoothBondResult) {
    val l = bondResult.bondResult2Notify(mac)
    life.onActive { addStateChangeListener(l) }
    life.onInactive { removeStateChangeListener(l) }
}
//</editor-fold>