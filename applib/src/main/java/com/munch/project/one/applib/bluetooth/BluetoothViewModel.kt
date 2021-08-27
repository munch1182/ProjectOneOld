package com.munch.project.one.applib.bluetooth

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.app.AppHelper
import com.munch.lib.base.toLive
import com.munch.lib.bluetooth.*
import com.munch.lib.fast.base.DataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/8/24 16:25.
 */
class BluetoothViewModel : ViewModel() {

    private var devList: MutableList<BtItemDev?> = mutableListOf()
    private val devs = MutableLiveData<MutableList<BtItemDev?>?>()
    fun devs() = devs.toLive()
    private val instance = BluetoothHelper.instance

    init {
        if (!instance.isInitialized) {
            instance.init(AppHelper.app)
        }
        instance.connectedDev?.let {
            devList.add(BtItemDev.from(it))
            devs.postValue(devList)
        }
    }

    private var currentConfig: BtActivityConfig = BtActivityConfig.config!!
    private val config = MutableLiveData(currentConfig)
    fun config() = config.toLive()

    private val notice = MutableLiveData("")
    fun notice() = notice.toLive()
    private val scanListener = object : OnScannerListener {
        private var start = 0
        private var bondedDevices: MutableList<BtItemDev>? = null
        override fun onStart() {
            currentConfig = config.value!!
            devList.clear()
            countDown()
            start = 0
            val bondedDevices = instance.set.getBondedDevices()
            if (bondedDevices.isNotEmpty()) {
                var connectBySystem: BtItemDev? = null
                this.bondedDevices = bondedDevices.filter { it.type == BluetoothType.Ble }
                    .map {
                        BtItemDev.from(it).apply {
                            if (isConnectedBySystem) {
                                connectBySystem = this
                            }
                        }
                    }
                    .toMutableList()
                devList.addAll(this.bondedDevices!!)
                if (connectBySystem != null) {
                    devList.remove(connectBySystem)
                    devList.add(0, connectBySystem)
                }
                devs.postValue(devList.toMutableList())
                start = devList.size
            }
        }

        private var end = false
        private var time = 0

        private fun countDown() {
            viewModelScope.launch(Dispatchers.IO) {
                end = false
                time = 0
                flow {
                    while (!end) {
                        time += 1
                        emit(time)
                        if (end) {
                            return@flow
                        }
                        delay(1000L)
                    }
                }.collect {
                    notice.postValue("已开始${time}s")
                }
            }
        }

        override fun onBatchScan(devices: MutableList<BluetoothDev>) {
            val devs = devices.filter { isValid(it) }
                .map { BtItemDev(it) }
                .filter {
                    var isNew = true
                    kotlin.run out@{
                        bondedDevices?.forEach { dev ->
                            if (it.dev.mac == dev.dev.mac) {
                                isNew = false
                                return@out
                            }
                        }
                    }
                    isNew
                }
            if (devs.isNotEmpty()) {
                devList.addAll(start, devs)
                this@BluetoothViewModel.devs.postValue(devList.toMutableList())
            }
        }

        override fun onScan(device: BluetoothDev) {
            val dev = BtItemDev(device)
            if (isValid(device)) {
                bondedDevices?.forEach {
                    if (it.dev.mac == dev.dev.mac) {
                        return
                    }
                }
                devList.add(start, dev)
                devs.postValue(devList.toMutableList())
            }
        }

        override fun onComplete(devices: MutableList<BluetoothDev>) {
            end = true
            end()
            notice.postValue("已结束，共扫描到${devices.size}个设备，历时${time}s")
        }

        override fun onFail() {
            end = true
            end()
            notice.postValue("出现错误，${time}s")
        }

        private fun end() {
            instance.scanListeners.remove(this)
        }
    }

    private fun isValid(dev: BluetoothDev): Boolean {
        if (currentConfig.noName && dev.name == null) {
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun toggleScan() {
        if (instance.state.isScanning) {
            instance.stopScan()
            //需要在回调中remove，否则可能会因为线程问题不会触发onComplete
            /*instance.scanListeners.remove(scanListener)*/
        } else {
            instance.scanListeners.add(scanListener)
            val config = config.value
            val timeout = (config?.timeOut ?: 0L) * 1000L
            val filter = config?.let { mutableListOf(ScanFilter(config.name, config.mac)) }
            instance.bleScanBuilder().setFilter(filter).setTimeout(timeout).startScan()
        }
    }

    override fun onCleared() {
        super.onCleared()
        instance.stopScan()
        instance.scanListeners.remove(scanListener)
    }

    fun toggleConnect(dev: BluetoothDev?) {
        dev ?: return
        if (instance.state.isConnected) {
            if (instance.connectedDev?.mac == dev.mac) {
                dev.disconnect()
            }
        } else {
            dev.connect()
        }
    }
}

data class BtActivityConfig(
    var type: BluetoothType = BluetoothType.Classic,
    var name: String? = null,
    var mac: String? = null,
    var timeOut: Long = 25L,
    var noName: Boolean = true,
    var connectAuto: Boolean = true
) {

    var isClassic: Boolean = type == BluetoothType.Classic
        set(value) {
            if (field == value) {
                return
            }
            field = value
            type = if (field) BluetoothType.Classic else BluetoothType.Ble
        }

    companion object {

        private const val KEY_BT_CONFIG = "key_bt_config"

        var config: BtActivityConfig? = null
            get() {
                return DataHelper.App.instance.get(KEY_BT_CONFIG, BtActivityConfig())
            }
            set(value) {
                DataHelper.App.instance.put(KEY_BT_CONFIG, value)
                field = value
            }
    }
}

data class BtItemDev(val dev: BluetoothDev) {

    companion object {

        fun from(dev: BluetoothDev): BtItemDev {
            return BtItemDev(dev).apply {
                isBond = dev.isBond()
                isConnectedBySystem = dev.isConnectedInSystem() ?: false
                isConnectedByHelper = dev == BluetoothHelper.instance.connectedDev
            }
        }
    }

    var isBond = false
    var isConnectedBySystem = false
    var isConnectedByHelper = false

    val state: String
        get() {
            if (!isBond && !isConnectedByHelper) {
                return ""
            }
            val sb = StringBuilder()
            if (isConnectedByHelper) {
                sb.append("当前已连接")
            }
            if (sb.isNotEmpty()) {
                sb.append(", ")
            }
            if (isConnectedBySystem) {
                sb.append("已被系统连接")
            } else if (isBond) {
                sb.append("已配对")
            }
            return sb.toString()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BtItemDev

        if (dev != other.dev) return false
        if (isBond != other.isBond) return false
        if (isConnectedBySystem != other.isConnectedBySystem) return false
        if (isConnectedByHelper != other.isConnectedByHelper) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dev.hashCode()
        result = 31 * result + isBond.hashCode()
        result = 31 * result + isConnectedBySystem.hashCode()
        result = 31 * result + isConnectedByHelper.hashCode()
        return result
    }

    override fun toString(): String {
        return "BtItemDev(dev=${dev.mac}, isBond=$isBond, isConnectedBySystem=$isConnectedBySystem, isConnectedByHelper=$isConnectedByHelper)"
    }
}
