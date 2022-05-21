package com.munch.project.one.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.bluetooth.*
import com.munch.lib.extend.LinearLineItemDecoration
import com.munch.lib.extend.bind
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.recyclerview.BaseBindViewHolder
import com.munch.lib.recyclerview.BindRVAdapter
import com.munch.lib.recyclerview.differ
import com.munch.lib.result.OnPermissionResultListener
import com.munch.lib.result.permission
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBluetoothBinding
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Created by munch1182 on 2022/5/18 21:20.
 */
@SuppressLint("SetTextI18n")
class BluetoothActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by bind<ActivityBluetoothBinding>()
    private val adapter = object : BindRVAdapter<BluetoothDev, ItemBluetoothBinding>(
        ItemBluetoothBinding::class,
        differ({ o, n -> o.rssi == n.rssi }, { o, n -> o.mac == n.mac })
    ) {
        override fun onBind(
            holder: BaseBindViewHolder<ItemBluetoothBinding>,
            position: Int,
            bean: BluetoothDev
        ) {
            holder.bind.apply {
                btDevMac.text = bean.mac
                btDevName.text = bean.name?.takeIf { it.isNotEmpty() } ?: "N/A"
                "${bean.rssi}dbm".also { btDevRssi.text = it }
            }
        }

    }
    private val instance = BluetoothHelper.instance


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instance.isScanning.observe(this) {
            if (it) {
                bind.btScan.text = "SCAN STOP"
                bind.btScan.setOnClickListener {
                    checkOrRequest { instance.stop() }
                }
            } else {
                bind.btScan.text = "SCAN START"
                bind.btScan.setOnClickListener {
                    checkOrRequest { instance.scan() }
                }
            }
        }

        instance.setScanOnResume(this, object : ScanListener {
            override fun onScanned(
                dev: BluetoothDev,
                map: LinkedHashMap<String, BluetoothDev>
            ) {
                adapter.set(map.values.toList())
            }
        })

        bind.btRv.apply {
            val lm = LinearLayoutManager(this@BluetoothActivity)
            layoutManager = lm
            addItemDecoration(LinearLineItemDecoration(lm))
            adapter = this@BluetoothActivity.adapter
        }
    }

    private class TheHandler : OnConnectHandler {
        override suspend fun onConnect(
            connector: Connector,
            gatt: BluetoothGatt,
            dispatcher: GattCallbackDispatcher
        ): Boolean {
            return suspendCancellableCoroutine { c ->
                runBlocking {
                    var g = dispatcher.discoverService()
                    var index = 5
                    while (g == null && index > 0) {
                        index--
                        g = dispatcher.discoverService()
                    }
                    if (g == null) {
                        c.resume(false)
                        return@runBlocking
                    }
                    val mtu = 247
                    index = 5
                    var requestMtu = dispatcher.requestMtu(mtu)
                    while (requestMtu != mtu && index > 0) {
                        index--
                        requestMtu = dispatcher.requestMtu(mtu)
                    }
                    if (requestMtu == null) {
                        c.resume(false)
                        return@runBlocking
                    }
                    c.resume(true)
                }
            }
        }
    }

    private fun checkOrRequest(grant: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permission(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            permission(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }.request(object : OnPermissionResultListener {
            override fun onPermissionResult(
                isGrantAll: Boolean,
                grantedArray: Array<String>,
                deniedArray: Array<String>
            ) {
                grant.invoke()
            }
        })
    }
}