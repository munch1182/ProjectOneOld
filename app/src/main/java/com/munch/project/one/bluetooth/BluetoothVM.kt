package com.munch.project.one.bluetooth

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.munch.lib.android.extend.ContractVM
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.connect.*
import com.munch.lib.bluetooth.dev.BluetoothType
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

    init {
        BluetoothHelper.watchScan(this) { post(STATE.IsScan(it)) }
        BluetoothHelper.watchDevsScan(this) { post(STATE.ScannedDevs(it)) }
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
                val judge = BluetoothConnector.Builder().judge(object : IBluetoothConnectJudge {
                    override suspend fun onJudge(gatt: BluetoothGattHelper): BluetoothConnectResult {
                        if (!gatt.discoverServices()) {
                            return BluetoothConnectFailReason.CustomErr(1).toReason()
                        }
                        val main =
                            gatt.getService(UUID.fromString("461c5198-449c-449b-9fe5-6259dc3fcbed"))
                        val writer =
                            main?.getCharacteristic(UUID.fromString("461c0028-449c-449b-9fe5-6259dc3fcbed"))
                        if (writer != null) {
                            gatt.setDataWriter(writer)
                        }
                        if (main == null || writer == null) {
                            return BluetoothConnectFailReason.CustomErr(3).toReason()
                        }
                        if (gatt.requestMtu(247) != 247) {
                            return BluetoothConnectFailReason.CustomErr(2).toReason()
                        }
                        return BluetoothConnectResult.Success
                    }
                })
                if (!dev.connect(judge).isSuccess) {
                    return
                }
                dev.send(
                    byteArrayOf(
                        0x89.toByte(),
                        0x56,
                        0x12,
                        0x00,
                        0x01,
                        0x00,
                        0x00,
                        0x00,
                        0x23,
                        0x3B,
                        0x01,
                        0x00,
                        0x00,
                        0x00,
                        0x4B,
                        0xB7.toByte(),
                        0xB5.toByte(),
                        0x3A
                    )
                )
            }
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
}