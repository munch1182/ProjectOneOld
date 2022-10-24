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
import com.munch.lib.android.recyclerview.BaseBindViewHolder
import com.munch.lib.android.recyclerview.DifferAdapterFun
import com.munch.lib.android.recyclerview.SimpleBaseBindAdapter
import com.munch.lib.android.result.then
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothScanDev
import com.munch.lib.fast.view.dialog.DialogHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBluetoothBinding

/**
 * Create by munch1182 on 2022/9/30 10:09.
 */
class BluetoothActivity : BaseActivity(),
    ActivityDispatch by dispatchDef(),
    IDialogManager by DefaultDialogManager() {

    private val bind by bind<ActivityBluetoothBinding>()
    private val vm by get<BluetoothVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.bluetoothRv.apply { layoutManager = LinearLayoutManager(ctx) }
        val bluetoothAdapter = BluetoothAdapter()
        bind.bluetoothRv.adapter = bluetoothAdapter

        vm.state.observe(this) {
            when (it) {
                is BluetoothState.IsScan ->
                    menu?.get(0)?.title = if (it.isScan) "STOP SCANNING" else "SCAN"
                is BluetoothState.ScannedDevs -> bluetoothAdapter.set(it.data)
            }
        }
        bind.bluetoothFilter.setOnClickListener {
            DialogHelper.bottom()
                .content(BluetoothFilterView(this))
                .offer(this)
                .show()
        }
        addItem("SCAN") { withPermission { vm.dispatch(BluetoothIntent.ToggleScan) } }
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