package com.munch.project.one.bluetooth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.android.dialog.DefaultDialogManager
import com.munch.lib.android.dialog.IDialogManager
import com.munch.lib.android.dialog.offer
import com.munch.lib.android.extend.*
import com.munch.lib.android.log.log
import com.munch.lib.android.recyclerview.BaseBindViewHolder
import com.munch.lib.android.recyclerview.DifferAdapterFun
import com.munch.lib.android.recyclerview.SimpleBaseBindAdapter
import com.munch.lib.android.recyclerview.pos
import com.munch.lib.android.result.then
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothScanDev
import com.munch.lib.fast.view.dialog.DialogHelper
import com.munch.lib.fast.view.dialog.view
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBluetoothBinding
import com.munch.project.one.databinding.LayoutDialogBluetoothRecordBinding
import com.munch.project.one.bluetooth.BluetoothIntent as INTENT
import com.munch.project.one.bluetooth.BluetoothState as STATE

/**
 * Create by munch1182 on 2022/9/30 10:09.
 */
class BluetoothActivity : BaseActivity(),
    ActivityDispatch by dispatchDef(),
    IDialogManager by DefaultDialogManager() {

    private val bind by bind<ActivityBluetoothBinding>()
    private val vm by get<BluetoothVM>()
    private val view by lazy { BluetoothFilterView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.bluetoothRv.apply { layoutManager = LinearLayoutManager(ctx) }
        val bluetoothAdapter = BluetoothAdapter()
        bind.bluetoothRv.adapter = bluetoothAdapter

        vm.state.observe(this) {
            when (it) {
                is STATE.IsScan ->
                    menu?.get(0)?.title = if (it.isScan) "STOP SCANNING" else "SCAN"
                is STATE.ScannedDevs -> bluetoothAdapter.set(it.data)
                is STATE.FilterUpdate -> {
                    val filter = it.f.toViewFilter()
                    this.view.set(filter)
                    bind.bluetoothFilter.text = filter.toString()
                }
            }
        }
        bind.bluetoothFilter.setOnClickListener {
            DialogHelper.bottom()
                .view(view.apply { removeFromParent() })
                .onDismiss { vm.dispatch(INTENT.UpdateFilter(BluetoothFilter.from(get()))) }
                .offer(this)
                .show()
        }
        addItem("SCAN") { withPermission { vm.dispatch(INTENT.ToggleScan) } }

        bluetoothAdapter.setOnItemLongClick {

            vm.dispatch(INTENT.StopScan)

            val dev = bluetoothAdapter.get(it.pos)
            DialogHelper.bottom()
                .view<LayoutDialogBluetoothRecordBinding>(this)
                .onShow {
                    val str = "${dev.name ?: "N/A"}\n(${dev.mac})"
                    btRecordName.text = str.size(14, true, str.indexOf('('))
                    if (dev is BluetoothScanDev) {
                        val rawStr = dev.rawRecord
                            ?.joinToString("") { c -> c.toHexStr().uppercase() }
                        btRecordRaw.text = "0x${rawStr}"
                        dev.getRecords().forEach { r -> log(r) }
                    }
                }
                .offer(this)
                .show()
        }
    }

    private class BluetoothAdapter :
        SimpleBaseBindAdapter<BluetoothDev, ItemBluetoothBinding>(
            DifferAdapterFun(differ({ this.toOrNull<BluetoothScanDev>()?.rssi ?: hashCode() }))
        ) {
        override fun onBind(holder: BaseBindViewHolder<ItemBluetoothBinding>, bean: BluetoothDev) {
            holder.bind.apply {
                bluetoothTitle.text = bean.name ?: "N/A"
                bluetoothMac.text = bean.mac
                if (bean is BluetoothScanDev) {
                    bluetoothDbm.text = bean.rssiStr
                }
            }
        }
    }

    private fun withPermission(function: () -> Unit) {
        then(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
            .then(
                {
                    getSystemService(Context.LOCATION_SERVICE).to<LocationManager>()
                        .isProviderEnabled(LocationManager.GPS_PROVIDER)
                },
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            )
            .then(
                { BluetoothHelper.isEnable },
                Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
            .start {
                if (!it) return@start
                function.invoke()
            }
    }
}