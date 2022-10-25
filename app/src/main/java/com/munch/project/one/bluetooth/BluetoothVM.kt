package com.munch.project.one.bluetooth

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.munch.lib.android.extend.ContractVM
import com.munch.lib.android.log.log
import com.munch.lib.bluetooth.*
import com.munch.lib.fast.view.data.DataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.munch.project.one.bluetooth.BluetoothState as STATE
import com.munch.project.one.bluetooth.BluetoothIntent as INTENT

/**
 * Create by munch1182 on 2022/10/22 11:52.
 */
class BluetoothVM : ContractVM<INTENT, STATE>() {

    private lateinit var currFilter: BluetoothFilter

    init {
        BluetoothHelper.watchScan(this) { post(STATE.IsScan(it)) }
        BluetoothHelper.setDevsScan(this) { post(STATE.ScannedDevs(it)) }
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
            INTENT.StartScan -> BluetoothHelper.stopThenStartScan()
            INTENT.ToggleScan -> {
                if (BluetoothHelper.isScanning) {
                    BluetoothHelper.stopScan()
                } else {
                    post(STATE.ScannedDevs(listOf()))
                    BluetoothHelper.startScan()
                }
            }
            is INTENT.UpdateFilter -> {
                val f = it.f
                log(f, currFilter)
                if (f == currFilter) {
                    return
                }
                if (BluetoothHelper.isScanning) { // 扫描中更改filter, 重新扫描
                    post(STATE.ScannedDevs(listOf()))
                    BluetoothHelper.stopThenStartScan()
                }
                updateFilter(f)
                saveFilter(f)
                post(STATE.FilterUpdate(it.f))
            }
        }
    }

    private fun updateFilter(f: BluetoothFilter) {
        log(f.to())
        BluetoothHelper.configScan { f.to()?.let { filter(it) } ?: noFilter() }
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