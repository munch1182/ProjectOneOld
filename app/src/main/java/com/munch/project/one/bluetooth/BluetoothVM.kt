package com.munch.project.one.bluetooth

import android.bluetooth.BluetoothGattDescriptor
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.munch.lib.android.extend.ContractVM
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.connect.*
import com.munch.lib.bluetooth.data.BluetoothDataReceiver
import com.munch.lib.bluetooth.dev.BluetoothDev
import com.munch.lib.bluetooth.dev.BluetoothType
import com.munch.lib.bluetooth.helper.setConnectListener
import com.munch.lib.bluetooth.helper.stopThenStartScan
import com.munch.lib.bluetooth.helper.watchDevsScan
import com.munch.lib.bluetooth.helper.watchScan
import com.munch.lib.fast.view.data.DataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import com.munch.project.one.bluetooth.BluetoothIntent as INTENT
import com.munch.project.one.bluetooth.BluetoothState as STATE

/**
 * Create by munch1182 on 2022/10/22 11:52.
 */
class BluetoothVM : ContractVM<INTENT, STATE>() {

    companion object {
        const val SHARE_NAME = "bluetooth"
    }

    private lateinit var currFilter: BluetoothFilter
    private var targetDev: BluetoothDev? = null

    init {
        BluetoothHelper.watchScan(this) { post(STATE.IsScan(it)) }
        BluetoothHelper.watchDevsScan(this) {
            if (!BluetoothHelper.isScanning) return@watchDevsScan
            post(STATE.ScannedDevs(it))
        }
        BluetoothHelper.config {
            enableLog(true, originData = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val filter = BluetoothFilterHelper.get()
            currFilter = filter
            updateFilter(filter)
            post(STATE.FilterUpdate(filter))
        }
    }

    override suspend fun onCollect(it: INTENT) {
        when (it) {
            INTENT.StopScan -> BluetoothHelper.stopScan()
            INTENT.StartScan -> {
                post(STATE.ScannedDevs(listOf()))
                BluetoothHelper.stopThenStartScan()
            }
            INTENT.ToggleScan -> {
                if (BluetoothHelper.isScanning) {
                    onCollect(INTENT.StopScan)
                } else {
                    onCollect(INTENT.StartScan)
                }
            }
            is INTENT.UpdateFilter -> {
                val f = it.f
                if (f == currFilter) {
                    return
                }
                currFilter = f
                if (BluetoothHelper.isScanning) { // 扫描中更改filter, 重新扫描
                    onCollect(INTENT.StartScan)
                }
                updateFilter(f)
                saveFilter(f)
                post(STATE.FilterUpdate(it.f))
            }
            is INTENT.Connect -> {
                val dev = it.dev
                targetDev = dev
                dev.setConnectListener(this, listener)
                if (dev.isDisconnected) {
                    dev.connect()
                }
            }
            INTENT.ToggleConnect -> {
                val dev = targetDev ?: return
                if (dev.isConnected) {
                    dev.disconnect()
                } else if (dev.isDisconnected) {
                    onCollect(INTENT.Connect(dev))
                }
            }
        }
    }

    private val listener = object : OnBluetoothConnectStateListener {
        override fun onConnectState(
            state: BluetoothConnectState,
            last: BluetoothConnectState,
            dev: BluetoothDev
        ) {
            post(STATE.ConnectDev(dev, state))
            if (state.isDisconnected) {
                dev.removeConnectListener(this)
                post(STATE.ShowContent(""))
            } else if (state.isConnected) {
                discoverContent(dev)
            }
        }
    }

    private fun discoverContent(dev: BluetoothDev) {
        viewModelScope.launch(Dispatchers.IO) {
            val helper = dev.toScanned()?.operate as? BluetoothGattHelper ?: return@launch
            if (!helper.discoverServices()) return@launch
            val gatt = helper.gatt ?: return@launch
            val sb = StringBuilder()
            gatt.services.forEach {
                sb.append(it.uuid.toString())
                sb.append("\n")
                it.characteristics.forEach { c ->
                    sb.append("⤷").append(c.uuid.toString())
                    sb.append("\n")
                }
            }
            post(STATE.ShowContent(sb.toString()))
        }
    }

    private fun updateFilter(f: BluetoothFilter) {
        BluetoothHelper.configDefaultScan {
            type(if (f.isBle) BluetoothType.LE else BluetoothType.CLASSIC)
            filter(f.to())
        }
    }

    private fun saveFilter(f: BluetoothFilter) {
        viewModelScope.launch(Dispatchers.IO) { BluetoothFilterHelper.save(f) }
    }

    object BluetoothFilterHelper : DataHelper() {
        private const val KEY_BLUETOOTH_FILTER = "KEY_BLUETOOTH_FILTER"

        suspend fun save(f: BluetoothFilter) {
            put(KEY_BLUETOOTH_FILTER, Gson().toJson(f))
        }

        suspend fun get() =
            get(KEY_BLUETOOTH_FILTER, "{}")
                ?.let { Gson().fromJson(it, BluetoothFilter::class.java) }
                ?: BluetoothFilter()
    }


    private object Watch : IBluetoothLeConnectJudge, BluetoothDataReceiver {
        override suspend fun onLeJudge(gatt: BluetoothGattHelper): BluetoothConnectResult {
            if (!gatt.discoverServices()) {
                return BluetoothConnectFailReason.CustomErr(1).toReason()
            }
            val main =
                gatt.getService(UUID.fromString(""))
            val writer =
                main?.getCharacteristic(UUID.fromString(""))
            if (writer != null) {
                gatt.setDataWriter(writer)
            }
            val notify =
                main?.getCharacteristic(UUID.fromString(""))
            val notifyDesc =
                notify?.getDescriptor(UUID.fromString(""))
            if (main == null || writer == null || notify == null || notifyDesc == null) {
                return BluetoothConnectFailReason.CustomErr(3).toReason()
            }
            if (gatt.setCharacteristicNotification(notify, true) != true) {
                return BluetoothConnectFailReason.CustomErr(4).toReason()
            }
            notifyDesc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            if (!gatt.writeDescriptor(notifyDesc)) {
                return BluetoothConnectFailReason.CustomErr(5).toReason()
            }
            if (gatt.requestMtu(247) != 247) {
                return BluetoothConnectFailReason.CustomErr(2).toReason()
            }
            return BluetoothConnectResult.Success
        }

        override suspend fun onReceived(data: ByteArray) {
        }

    }
}