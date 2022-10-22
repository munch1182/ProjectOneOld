package com.munch.lib.bluetooth

import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.define.Update
import com.munch.lib.android.helper.ILifecycle
import com.munch.lib.android.helper.toILifecycle

/**
 * Create by munch1182 on 2022/10/21 17:48.
 */

/**
 * 自动添加和移除设备扫描回调
 *
 * @see setDevsScan
 */
fun BluetoothHelper.setScan(lifecycle: ILifecycle, listener: OnBluetoothDevScannedListener) {
    lifecycle.onActive { addScanListener(listener) }
    lifecycle.onInactive { removeScanListener(listener) }
}

/**
 * 自动添加和移除设备扫描回调
 *
 * 与[setScan]相同, 但是回调的是已扫描到所有设备
 */
fun BluetoothHelper.setDevsScan(lifecycle: ILifecycle, listener: OnBluetoothDevsScannedListener) {
    setScan(lifecycle, listener.toDevScanned())
}

/**
 * 自动添加和移除扫描状态变更回调
 */
fun BluetoothHelper.watchScan(lifecycle: ILifecycle, onUpdate: Update<Boolean>) {
    setScan(lifecycle, object : OnBluetoothDevScanListener {
        override fun onScanStart() {
            onUpdate.invoke(true)
        }

        override fun onScanStop() {
            onUpdate.invoke(false)
        }

        override fun onDevScanned(dev: BluetoothDev) {
        }

    })
}

/**
 * 自动添加和移除蓝牙通知回调
 */
fun BluetoothHelper.watchState(lifecycle: ILifecycle, listener: OnBluetoothStateNotifyListener) {
    lifecycle.onActive { addStateChangeListener(listener) }
    lifecycle.onInactive { removeStateChangeListener(listener) }
}

fun interface OnBluetoothDevsScannedListener {
    /**
     * 当扫描到一个设备的回调, 但返回的是扫描到的所有设备
     */
    fun onDevScanned(dev: List<BluetoothDev>)
}

fun OnBluetoothDevsScannedListener.toDevScanned(): OnBluetoothDevScannedListener =
    BluetoothDevsScannedListenerWrapper(this)

/**
 * 将[OnBluetoothDevsScannedListener]实现并转为[OnBluetoothDevScannedListener]
 */
class BluetoothDevsScannedListenerWrapper(private val devs: OnBluetoothDevsScannedListener) :
    OnBluetoothDevScannedListener {

    private val map = LinkedHashMap<String, BluetoothDev>()

    override fun onDevScanned(dev: BluetoothDev) {
        map[dev.mac] = dev
        devs.onDevScanned(map.values.toList())
    }
}

//<editor-fold desc="LifecycleOwner">
fun BluetoothHelper.setScan(lifecycle: LifecycleOwner, listener: OnBluetoothDevScannedListener) {
    setScan(lifecycle.toILifecycle(), listener)
}

fun BluetoothHelper.setDevsScan(
    lifecycle: LifecycleOwner,
    listener: OnBluetoothDevsScannedListener
) {
    setDevsScan(lifecycle.toILifecycle(), listener)
}

fun BluetoothHelper.watchScan(lifecycle: LifecycleOwner, onUpdate: Update<Boolean>) {
    watchScan(lifecycle.toILifecycle(), onUpdate)
}

fun BluetoothHelper.watchState(
    lifecycle: LifecycleOwner,
    listener: OnBluetoothStateNotifyListener
) {
    watchState(lifecycle.toILifecycle(), listener)
}
//</editor-fold>