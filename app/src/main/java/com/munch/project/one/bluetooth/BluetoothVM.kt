package com.munch.project.one.bluetooth

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.munch.lib.android.extend.ContractVM
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.dev.BluetoothType
import com.munch.lib.bluetooth.helper.stopThenStartScan
import com.munch.lib.bluetooth.helper.watchDevsScan
import com.munch.lib.bluetooth.helper.watchScan
import com.munch.lib.fast.view.data.DataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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