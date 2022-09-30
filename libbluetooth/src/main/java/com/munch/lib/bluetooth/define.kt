package com.munch.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import com.munch.lib.android.extend.SealedClassToStringByName
import com.munch.lib.android.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/9/29 14:59.
 */
/**
 * 一个拥有正确mac地址的对象即一个蓝牙对象
 */
interface IBluetoothDev {
    val mac: String
}

/**
 * 蓝牙设备的类型
 */
sealed class BluetoothType : SealedClassToStringByName() {
    object UNKNOWN : BluetoothType()
    object CLASSIC : BluetoothType()
    object LE : BluetoothType()
    object DUAL : BluetoothType()

    fun from(dev: BluetoothDevice): BluetoothType {
        return when (dev.type) {
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> UNKNOWN
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> CLASSIC
            BluetoothDevice.DEVICE_TYPE_LE -> LE
            BluetoothDevice.DEVICE_TYPE_DUAL -> DUAL
            else -> UNKNOWN
        }
    }
}

/**
 * 提供蓝牙管理对象
 */
interface IBluetoothManager {
    /**
     * 提供一个系统的BluetoothManager对象
     */
    val bm: BluetoothManager?

    /**
     * 提供一个系统的BluetoothAdapter对象
     */
    val adapter: BluetoothAdapter?
}

/**
 * 提供蓝牙相关状态
 */
interface IBluetoothState {
    /**
     * 是否支持BLE
     */
    val isSupportBle: Boolean

    /**
     * 蓝牙是否开启
     */
    val isEnable: Boolean

    /**
     * 系统蓝牙已配对设备
     */
    val pairedDevs: Set<BluetoothDevice>?

    /**
     * 回调蓝牙通知
     */
    fun addStateChangeListener(l: OnBluetoothStateNotifyListener?)
    fun removeStateChangeListener(l: OnBluetoothStateNotifyListener?)
}

/**
 * 进行蓝牙扫描
 *
 * 注意: 蓝牙扫描活动是唯一的, 不允许同时进行多个扫描
 */
interface IBluetoothScanner : IBluetoothManager {

    /**
     * 当前是否正在扫描
     *
     * 注意: 这个值只能代表当前Scanner是否正在扫描中
     * 注意: [find]虽然使用了扫描程序, 但不算在正在扫描中, 也不影响扫描的开关
     */
    val isScanning: Boolean

    fun setScanFilter(filter: OnBluetoothDevFilter?): IBluetoothScanner

    /**
     * 开始扫描设备, 会更改[isScanning]的状态
     */
    fun startScan()

    /**
     * 停止扫描, 会更改[isScanning]的状态
     */
    fun stopScan()

    /**
     * 添加蓝牙扫描回调
     *
     * 注意: [find]方法不会触发回调
     *
     * @see OnBluetoothDevScanListener
     */
    fun addScanListener(l: OnBluetoothDevScannedListener?)
    fun removeScanListener(l: OnBluetoothDevScannedListener?)
}

/**
 * 进行蓝牙连接
 */
interface IBluetoothConnector {

    /**
     * 执行连接
     *
     * @param timeout 超时时间, 超出这个时间仍未被连接, 则返回连接失败
     *
     * @return 同步返回连接结果
     */
    suspend fun connect(timeout: Long = 30000L): Boolean

    /**
     * 断开连接
     * @return 同步返回断开结果
     */
    suspend fun disconnect(): Boolean

    /**
     * 添加蓝牙连接回调
     *
     * @see OnBluetoothDevConnectListener
     */
    fun addConnectListener(l: OnBluetoothDevConnectResultListener?)
    fun removeConnectListener(l: OnBluetoothDevConnectResultListener?)
}

//<editor-fold desc="imp">
/**
 * 提供蓝牙相关的通用方法
 */
internal interface IBluetoothFun : CoroutineScope {

    val log: Logger
        get() = BluetoothHelper.log

    override val coroutineContext: CoroutineContext
        get() = BluetoothHelper

}

/**
 * 系统蓝牙的状态变更通知
 */
sealed class BluetoothStateNotify : SealedClassToStringByName() {
    /**
     * 蓝牙打开的通知
     */
    object StateOn : BluetoothStateNotify()

    /**
     * 蓝牙关闭的通知
     */
    object StateOff : BluetoothStateNotify()

    /**
     * 蓝牙绑定成功的通知
     */
    object Bonded : BluetoothStateNotify()

    /**
     * 蓝牙绑定中的通知
     */
    object Bonding : BluetoothStateNotify()

    /**
     * 蓝牙绑定失败的通知
     */
    object BondFail : BluetoothStateNotify()
}

/**
 * 蓝牙通知的回调
 */
fun interface OnBluetoothStateNotifyListener {
    /**
     * 当收到系统蓝牙的更改时进行通知
     */
    fun onStateNotify(state: BluetoothStateNotify, mac: String?)
}

/**
 * 对蓝牙结果进行过滤
 */
fun interface OnBluetoothDevFilter {
    /**
     * 该设备是否需要被过滤, 不回调到设备扫描列表中
     *
     * true则不被过滤, false则被过滤掉
     */
    fun isDevNeedFiltered(dev: IBluetoothDev): Boolean
}

/**
 * 为了寻找特定[mac]地址的设备, 过滤掉其它所有的设备
 */
class BluetoothDevFindFilter(private val mac: String) : OnBluetoothDevFilter {
    override fun isDevNeedFiltered(dev: IBluetoothDev): Boolean {
        return dev.mac != mac
    }

}

/**
 * 蓝牙扫描结果回调
 */
fun interface OnBluetoothDevScannedListener {
    /**
     * 当扫描到一个设备的回调
     */
    fun onDevScanned(dev: IBluetoothDev)
}

/**
 * 蓝牙扫描回调
 */
interface OnBluetoothDevScanListener : OnBluetoothDevScannedListener {
    /**
     * 当蓝牙扫描开始
     */
    fun onScanStart()

    /**
     * 当蓝牙扫描结束
     */
    fun onScanStop()
}

/**
 * 蓝牙连接结果回调
 */
fun interface OnBluetoothDevConnectResultListener {

    /**
     * 当蓝牙连接成功或者失败的回调
     */
    fun onConnectResult(isSuccess: Boolean)
}

/**
 * 蓝牙连接回调
 */
interface OnBluetoothDevConnectListener : OnBluetoothDevConnectResultListener {
    /**
     * 当蓝牙连接开始
     */
    fun onConnectStart()

    /**
     * 当蓝牙连接结束(成功、超时或者失败)
     */
    fun onConnectStop()
}
//</editor-fold>