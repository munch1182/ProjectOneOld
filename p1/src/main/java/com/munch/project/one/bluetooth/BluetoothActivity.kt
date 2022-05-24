package com.munch.project.one.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.OnCancel
import com.munch.lib.bluetooth.*
import com.munch.lib.extend.LinearLineItemDecoration
import com.munch.lib.extend.bind
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.notice.Chose
import com.munch.lib.notice.OnSelect
import com.munch.lib.recyclerview.BaseBindViewHolder
import com.munch.lib.recyclerview.BindRVAdapter
import com.munch.lib.recyclerview.differ
import com.munch.lib.recyclerview.setOnItemClickListener
import com.munch.lib.result.*
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBluetoothBinding
import kotlinx.coroutines.launch
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

        adapter.setOnItemClickListener { _, pos, _ ->
            instance.stop()
            val dev = adapter.get(pos)
            lifecycleScope.launch {
                dev?.createBond()
            }
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
            override fun onPermissionResult(isGrantAll: Boolean, result: Map<String, Boolean>) {
                if (isGrantAll) {
                    judgeIntent({ c ->
                        val lm =
                            c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    }, Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        .explain { GPSExplain(it) }
                        .start { c ->
                            if (c) {
                                judgeIntent(
                                    { instance.isEnable },
                                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                ).delay(1500L)
                                    .start {
                                        if (it) {
                                            grant.invoke()
                                        }
                                    }
                            }
                        }

                }
            }
        })
    }

    private class GPSExplain(private val context: Context) : ExplainIntentNotice {

        private val db = AlertDialog.Builder(context)
        private var dialog: AlertDialog? = null
            get() = field ?: db.create().also { field = it }

        override fun onIntentExplain(): Boolean {
            db.setMessage("搜寻蓝牙设备需要GPS服务")
            return true
        }

        override fun show() {
            dialog?.show()
        }

        override fun cancel() {
            dialog?.cancel()
            dialog = null
        }

        override fun addOnCancel(onCancel: OnCancel?): GPSExplain {
            dialog?.setOnCancelListener { onCancel?.invoke() }
            return this
        }

        override fun addOnSelect(chose: OnSelect): GPSExplain {
            dialog?.setButton(
                DialogInterface.BUTTON_POSITIVE,
                context.getString(android.R.string.ok)
            ) { d, _ ->
                d.cancel()
                chose.invoke(Chose.Ok)
            }
            return this
        }

        override val isShowing: Boolean
            get() = dialog?.isShowing ?: false
    }
}