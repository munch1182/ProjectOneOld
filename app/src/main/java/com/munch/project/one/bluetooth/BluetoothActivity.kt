package com.munch.project.one.bluetooth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.android.extend.bind
import com.munch.lib.android.extend.ctx
import com.munch.lib.android.extend.to
import com.munch.lib.android.extend.toast
import com.munch.lib.android.recyclerview.BaseBindViewHolder
import com.munch.lib.android.recyclerview.SimpleBaseBindAdapter
import com.munch.lib.android.result.then
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBluetoothBinding

/**
 * Create by munch1182 on 2022/9/30 10:09.
 */
class BluetoothActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by bind<ActivityBluetoothBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.bluetoothRv.apply {
            layoutManager = LinearLayoutManager(ctx)
            //addItemDecoration(LinearLineItemDecoration())
        }
        val bluetoothAdapter =
            object : SimpleBaseBindAdapter<BluetoothDev, ItemBluetoothBinding>() {
                override fun onBind(
                    holder: BaseBindViewHolder<ItemBluetoothBinding>,
                    bean: BluetoothDev
                ) {
                    holder.bind.apply {
                        bluetoothTitle.text = "N/A"
                        bluetoothMac.text = bean.mac
                    }
                }
            }
        bind.bluetoothRv.adapter = bluetoothAdapter

        /*BluetoothLeScanner.set(this) { impInMain { bluetoothAdapter.add(it.to<BluetoothDev>()) } }
        withPermission { BluetoothLeScanner.startScan() }*/

        bluetoothAdapter.set(MutableList(30) {
            BluetoothDev("$it$it:$it$it:$it$it:$it$it:$it$it:$it$it")
        })
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
                Intent("android.bluetooth.adapter.action.REQUEST_ENABLE")
            )
            .start {
                if (!it) {
                    toast("cannot start scan")
                } else {
                    function.invoke()
                }
            }
    }
}