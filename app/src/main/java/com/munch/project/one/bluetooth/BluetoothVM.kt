package com.munch.project.one.bluetooth

import androidx.lifecycle.MutableLiveData
import com.munch.lib.android.extend.ContractVM
import com.munch.lib.android.extend.immutable
import com.munch.lib.bluetooth.*

/**
 * Create by munch1182 on 2022/10/22 11:52.
 */
class BluetoothVM : ContractVM<BluetoothIntent, BluetoothState>() {

    private val _filter = MutableLiveData(BluetoothFilter())
    val filter = _filter.immutable()

    init {
        BluetoothHelper.configScan { filter(BluetoothDevNoNameFilter()) }
        BluetoothHelper.watchScan(this) { post(BluetoothState.IsScan(it)) }
        BluetoothHelper.setDevsScan(this) { post(BluetoothState.ScannedDevs(it)) }
    }

    override suspend fun onCollect(it: BluetoothIntent) {
        when (it) {
            BluetoothIntent.StopScan -> BluetoothHelper.stopScan()
            BluetoothIntent.StartScan -> BluetoothHelper.stopThenStartScan()
            BluetoothIntent.ToggleScan -> {
                if (BluetoothHelper.isScanning) {
                    BluetoothHelper.stopScan()
                } else {
                    post(BluetoothState.ScannedDevs(listOf()))
                    BluetoothHelper.startScan()
                }
            }
            is BluetoothIntent.Filter -> {
                val f = it.f
                saveFilter(f)
                _filter.postValue(f)
            }
        }
    }

    private fun saveFilter(f: BluetoothFilter) {

    }

}