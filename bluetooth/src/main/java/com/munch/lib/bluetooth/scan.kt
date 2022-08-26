package com.munch.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import com.munch.lib.OnReceive
import com.munch.lib.extend.catch
import com.munch.lib.helper.ARSHelper
import com.munch.lib.receiver.ReceiverHelper

fun interface OnDeviceFilter {

    /**
     * 对设备进行过滤
     *
     * @return 设备是否需要被过滤, 如果返回true, 则该设备会被过滤掉, 不会回调到结果中
     */
    fun isDeviceNeedFilter(dev: IBluetoothDev): Boolean
}

object OnDeviceNoneFilter : OnDeviceFilter {
    override fun isDeviceNeedFilter(dev: IBluetoothDev) = false
}

interface OnDeviceScanListener {
    fun onDeviceScanStart() {}
    fun onDeviceScanned(dev: BluetoothScanDev)
    fun onDeviceScanComplete() {}
}

interface Scanner {

    fun setScanType(type: BluetoothType): Scanner

    /**
     * 扫描蓝牙设备
     *
     * @param filter 对扫描到的设备进行过滤, 被过滤的设备不会回调到结果中
     * @param listener 一次设备扫描回调, 当扫描结果时, 此回调会被移除
     */
    fun startScan(
        filter: OnDeviceFilter = OnDeviceNoneFilter,
        listener: OnDeviceScanListener? = null
    )

    fun stopScan()

    fun addDeviceScanListener(listener: OnDeviceScanListener)
    fun removeDeviceScanListener(listener: OnDeviceScanListener)
}

class ScannerImp(private val env: BluetoothEnv) : Scanner {

    private var scanner: Scanner? = null

    override fun setScanType(type: BluetoothType): Scanner {
        scanner = when (type) {
            BluetoothType.CLASSIC -> CLASSICScanner(env)
            BluetoothType.LE -> LEScanner(env)
            else -> throw IllegalStateException("unsupported: $type")
        }
        return this
    }

    override fun startScan(filter: OnDeviceFilter, listener: OnDeviceScanListener?) {
        if (scanner == null) {
            scanner = LEScanner(env)
        }
        scanner?.startScan(filter, listener)
    }

    override fun stopScan() {
        scanner?.stopScan()
    }

    override fun addDeviceScanListener(listener: OnDeviceScanListener) {
        scanner?.addDeviceScanListener(listener)
    }

    override fun removeDeviceScanListener(listener: OnDeviceScanListener) {
        scanner?.removeDeviceScanListener(listener)
    }
}

class LEScanner(private val env: BluetoothEnv) : Scanner, ARSHelper<OnDeviceScanListener?>() {
    private val log = BluetoothHelper.instance.log

    override fun setScanType(type: BluetoothType): Scanner {
        throw IllegalStateException()
    }

    override fun startScan(filter: OnDeviceFilter, listener: OnDeviceScanListener?) {
        log.log { "start LE SCAN." }
        env.adapter?.bluetoothLeScanner?.startScan(null, env.scanSetting, object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                listener?.onDeviceScanComplete()
                notifyUpdate { it?.onDeviceScanComplete() }
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val dev = BluetoothScanDev(result ?: return)
                if (!filter.isDeviceNeedFilter(dev)) {
                    listener?.onDeviceScanned(dev)
                    notifyUpdate { it?.onDeviceScanned(dev) }
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                results?.forEach { onScanResult(0, it) }
            }

        })
    }

    override fun stopScan() {
        notifyUpdate { it?.onDeviceScanComplete() }
    }

    override fun addDeviceScanListener(listener: OnDeviceScanListener) {
        add(listener)
    }

    override fun removeDeviceScanListener(listener: OnDeviceScanListener) {
        remove(listener)
    }
}

class CLASSICScanner(private val env: BluetoothEnv) : Scanner, ARSHelper<OnDeviceScanListener?>() {

    private var receiver: BLUEReceiver? = null

    override fun setScanType(type: BluetoothType): Scanner {
        throw IllegalStateException()
    }

    override fun startScan(filter: OnDeviceFilter, listener: OnDeviceScanListener?) {
        if (receiver != null) {
            throw IllegalStateException("must stop after scan.")
        }
        receiver = BLUEReceiver(env, listener)
        catch { receiver?.register() }
        env.adapter?.startDiscovery()
    }

    override fun stopScan() {
        env.adapter?.cancelDiscovery()
        catch { receiver?.unregister() }
        receiver = null
    }

    override fun addDeviceScanListener(listener: OnDeviceScanListener) {
        add(listener)
    }

    override fun removeDeviceScanListener(listener: OnDeviceScanListener) {
        remove(listener)
    }

    class BLUEReceiver(context: Context, private val listener: OnDeviceScanListener?) :
        ReceiverHelper<OnReceive<BluetoothDev>>(
            context,
            arrayOf(
                BluetoothDevice.ACTION_FOUND,
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
                BluetoothAdapter.ACTION_DISCOVERY_STARTED
            )
        ) {

        private val log = BluetoothHelper.instance.log

        override fun handleAction(context: Context, action: String, intent: Intent) {

            val actionStr = action.replace("android.bluetooth.device.action.", "")
                .replace("android.bluetooth.adapter.action.", "")

            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            ?: return
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, 0).toInt()
                    val dev = BluetoothScanDev(device, BluetoothType.CLASSIC, rssi)
                    log.log { "[${dev.mac}] find CLASSIC device." }
                    listener?.onDeviceScanned(dev)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    log.log { "broadcast receive action: $actionStr." }
                    listener?.onDeviceScanStart()
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    log.log { "broadcast receive action: $actionStr." }
                    listener?.onDeviceScanStart()
                }
            }
        }
    }
}