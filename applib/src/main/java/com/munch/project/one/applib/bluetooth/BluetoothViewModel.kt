package com.munch.project.one.applib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.app.AppHelper
import com.munch.lib.base.toLive
import com.munch.lib.bluetooth.*
import com.munch.lib.fast.base.DataHelper
import com.munch.lib.log.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Create by munch1182 on 2021/8/24 16:25.
 */
class BluetoothViewModel : ViewModel() {

    private val devMap = LinkedHashMap<String, BtItemDev?>()
    private val devs = MutableLiveData<MutableList<BtItemDev?>?>()
    fun devs() = devs.toLive()
    private val instance = BluetoothHelper.instance

    init {
        if (!instance.isInitialized) {
            instance.init(AppHelper.app)
        }
        instance.connectedDev?.let {
            devMap[it.mac] = BtItemDev.from(it)
            devs.postValue(devMap.values.toMutableList())
        }
    }

    private var currentConfig: BtActivityConfig = BtActivityConfig.config!!
    private val config = MutableLiveData(currentConfig)
    fun config() = config.toLive()

    private val notice = MutableLiveData("")
    fun notice() = notice.toLive()
    private val scanListener = OnScanCallback()

    @SuppressLint("MissingPermission")
    fun toggleScan() {
        if (instance.state.isConnecting) {
            instance.disconnect()
        }
        if (instance.state.isScanning) {
            instance.stopScan()
            //需要在回调中remove，否则可能会因为线程问题不会触发onComplete
            /*instance.scanListeners.remove(scanListener)*/
        } else {
            instance.scanListeners.add(scanListener)
            val config = config.value ?: return
            BtActivityConfig.config = config
            val timeout = config.timeOut * 1000L
            val filter = mutableListOf(ScanFilter(config.name, config.mac))
            instance.scanBuilder(config.type)
                .setJustFirst(config.notUpdateScan)
                .setReportDelay(if (config.modeBatch) 500L else 0)
                .setFilter(filter)
                .setTimeout(timeout)
                .startScan()
        }
    }

    override fun onCleared() {
        super.onCleared()
        instance.stopScan()
        instance.scanListeners.remove(scanListener)
    }

    fun toggleConnect(dev: BluetoothDev?) {
        dev ?: return
        if (instance.state.isScanning) {
            instance.stopScan()
        }
        if (instance.state.isConnected) {
            if (instance.connectedDev?.mac == dev.mac) {
                dev.disconnect()
            }
        } else {
            if (dev.isBle) {
                BluetoothHelper.instance.connectBle(dev, config.value?.connectM2Phy ?: false)
            } else {
                dev.connect()
            }
        }
    }

    fun lockDev(dev: BluetoothDev?) {
        dev ?: return
        val c = currentConfig.apply {
            name = dev.name
            mac = dev.mac
        }
        BtActivityConfig.config = c
        config.postValue(c)
    }

    private inner class OnScanCallback : OnScannerListener {
        //使用Channel来处理同一时间扫描到过多设备导致rv的添加动画卡顿的情形
        private var channel: Channel<BtItemDev>? = null
        private val sortList = mutableListOf<String>()
        private var start = 0
        private var connectedDevices: MutableList<BluetoothDev>? = null
        override fun onStart() {
            currentConfig = config.value!!
            devMap.clear()
            sortList.clear()
            countDown()
            start = 0
            connectedDevices = instance.set.getConnectedDevice() ?: mutableListOf()
            instance.set.getBondedDevices()?.map { BtItemDev.from(it) }
                ?.sortedBy { it.stateVal }
                ?.let {
                    it.forEach { dev ->
                        val key = dev.dev.mac
                        sortList.add(key)
                        devMap[key] = dev
                    }
                    start = it.size
                    devs.postValue(it.toMutableList())
                }
            channel?.close()
            channel = Channel()
            var pulling = false
            viewModelScope.launch(Dispatchers.IO) {
                channel?.consumeEach { dev ->
                    val key = dev.dev.mac
                    if (!devMap.containsKey(key)) {
                        sortList.add(start, key)
                    }
                    devMap[key] = dev
                    if (!pulling) {
                        pulling = true
                        viewModelScope.launch(Dispatchers.IO) {
                            var size = sortList.size - (devs.value?.size ?: 0)
                            while (size != 0) {
                                log(size)
                                devs.postValue(sortList.map { devMap[it] }.toMutableList())
                                delay(max(min(200L * size, 200L), 500L))
                                size = sortList.size - (devs.value?.size ?: 0)
                            }
                            pulling = false
                        }
                    }
                }
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
            val devs = devices.filter { isValid(it) }.map { BtItemDev.from(it) }
            if (devs.isNotEmpty()) {
                viewModelScope.launch { devs.forEach { dev -> channel?.send(dev) } }
            }
        }

        override fun onScan(device: BluetoothDev) {
            val dev = BtItemDev.from(device)
            if (isValid(device)) {
                viewModelScope.launch { channel?.send(dev) }
            }
        }

        override fun onComplete() {
            end()
            notice.postValue("已结束，用时${time}s")
        }

        override fun onFail() {
            end()
            notice.postValue("出现错误，${time}s")
        }

        private fun end() {
            //因为结束扫描时可能还没消费完成
            /*channel?.close()*/
            end = true
            instance.scanListeners.remove(this)
        }

        private fun isValid(dev: BluetoothDev): Boolean {
            if (currentConfig.noName && dev.name == null) {
                return false
            }
            return true
        }
    }
}

@Parcelize
data class BtActivityConfig(
    var type: BluetoothType = BluetoothType.Classic,
    var name: String? = null,
    var mac: String? = null,
    var timeOut: Long = 25L,
    var noName: Boolean = true,
    var connectAuto: Boolean = true,
    var modeBatch: Boolean = false,
    var notUpdateScan: Boolean = true,
    var connectM2Phy: Boolean = false
) : Parcelable {

    val canBatchMode: Boolean
        get() = BluetoothHelper.instance.set.isScanBatchingSupported
    val canM2Connect: Boolean
        get() = BluetoothHelper.instance.set.isLe2MPhySupported

    @IgnoredOnParcel
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

        const val STATE_NONE = 0
        const val STATE_BONDING = 1
        const val STATE_BONDED = 2
        const val STATE_CONNECTING = 3
        const val STATE_CONNECTED = 4
        const val STATE_CONNECTED_BY_HELPER = 5

        fun from(dev: BluetoothDev) = BtItemDev(dev).apply { updateState() }
    }

    var stateVal: Int = STATE_NONE
    val rssi: Int
        get() = dev.rssi

    fun updateBinding() {
        stateVal = STATE_BONDING
    }

    fun updateConnecting() {
        stateVal = STATE_CONNECTING
    }

    fun updateState(): BtItemDev {
        stateVal = when {
            dev.isConnecting -> STATE_CONNECTING
            dev.isConnectedByHelper -> STATE_CONNECTED_BY_HELPER
            dev.isConnectedBySystem() == true -> STATE_CONNECTED
            dev.bondState == BluetoothDevice.BOND_BONDING -> STATE_BONDING
            dev.isBond -> STATE_BONDED
            else -> STATE_NONE
        }
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BtItemDev

        if (dev != other.dev) return false
        if (stateVal != other.stateVal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dev.hashCode()
        result = 31 * result + stateVal
        return result
    }

    override fun toString(): String {
        return "BtItemDev(dev=$dev, stateVal=$stateVal)"
    }

    val state: String
        get() {
            val sb = StringBuilder()
            when (stateVal) {
                STATE_CONNECTED_BY_HELPER -> sb.append("已连接")
                STATE_CONNECTING -> sb.append("连接中")
                STATE_CONNECTED -> sb.append("有连接")
                STATE_BONDING -> sb.append("绑定中")
                STATE_BONDED -> sb.append("已绑定")
                else -> sb.append("")
            }
            return sb.toString()
        }
}
