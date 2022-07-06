package com.munch.project.one.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.OnCancel
import com.munch.lib.bluetooth.*
import com.munch.lib.extend.LinearLineItemDecoration
import com.munch.lib.extend.bind
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.log.log
import com.munch.lib.notice.Notice
import com.munch.lib.notice.OnSelect
import com.munch.lib.notice.OnSelectOk
import com.munch.lib.recyclerview.BaseBindViewHolder
import com.munch.lib.recyclerview.BindRVAdapter
import com.munch.lib.recyclerview.differ
import com.munch.lib.recyclerview.setOnItemClickListener
import com.munch.lib.result.ExplainContactNotice
import com.munch.lib.result.contact
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBluetoothBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
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
    private val barText by lazy { TextView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addRight(barText)

        instance.isScanning.observe(this) {
            if (it) {
                barText.text = "SCAN STOP"
                barText.setOnClickListener { checkOrRequest { instance.stop() } }
            } else {
                barText.text = "SCAN START"
                barText.setOnClickListener {
                    checkOrRequest {
                        //instance.scan()
                        BluetoothDev("84:A2:20:A1:05:34").apply {
                            addConnectHandler(TheHandler())
                            lifecycleScope.launch(Dispatchers.IO) {
                                find()
                                connect()
                            }
                        }
                    }
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
            timeout: Long,
            dispatcher: GattCallbackDispatcher
        ): Boolean {
            return suspendCancellableCoroutine { c ->
                runBlocking {
                    log(dispatcher.gatt?.services?.size ?: 0)
                    var g = dispatcher.discoverService(timeout)
                    var index = 5
                    while (g == null && index > 0) {
                        index--
                        g = dispatcher.discoverService(timeout)
                    }

                    if (g == null) {
                        c.resume(false)
                        return@runBlocking
                    }

                    log(dispatcher.gatt?.services?.size ?: 0)

                    val hidService =
                        dispatcher.getService(UUID.fromString("00001812-0000-1000-8000-00805f9b34fb"))

                    if (hidService == null) {
                        c.resume(false)
                        return@runBlocking
                    }
                    val protocolMode =
                        hidService.getCharacteristic(UUID.fromString("00002a4e-0000-1000-8000-00805f9b34fb"))
                    if (protocolMode == null) {
                        c.resume(false)
                        return@runBlocking
                    }
                    val characteristic = dispatcher.readCharacteristic(protocolMode, timeout)
                    if (characteristic == null) {
                        c.resume(false)
                        return@runBlocking
                    }

                    val mtu = 247
                    index = 5
                    var requestMtu = dispatcher.requestMtu(mtu, timeout)
                    while (requestMtu != mtu && index > 0) {
                        index--
                        requestMtu = dispatcher.requestMtu(mtu, timeout)
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
            contact(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            contact(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }.contact(
            { instance.isEnable },
            { Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE) }
        ).contact(
            {
                val lm = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            },
            { Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS) }
        ).explain { GPSExplain(it) }
            .start {
                if (it) grant.invoke()
            }
    }

    private class GPSExplain(private val context: Context) : ExplainContactNotice {

        private val db = AlertDialog.Builder(context)
        private var dialog: AlertDialog? = null
            get() = field ?: db.create().also { field = it }

        override fun onIntentExplain(intent: Intent): Boolean {
            when (intent.action) {
                Settings.ACTION_LOCATION_SOURCE_SETTINGS -> db.setMessage("搜寻蓝牙设备需要GPS服务")
                BluetoothAdapter.ACTION_REQUEST_ENABLE -> db.setMessage("需要开启蓝牙")
                else -> return false
            }
            return true
        }

        override fun onPermissionExplain(
            isBeforeRequest: Boolean,
            permissions: Map<String, Boolean>
        ): Boolean {
            return false
        }

        override fun show() {
            dialog?.show()
        }

        override fun cancel() {
            dialog?.cancel()
            dialog = null
        }

        override fun addOnCancel(onCancel: OnCancel?): GPSExplain {
            dialog?.setOnCancelListener {
                onCancel?.invoke()
                dialog = null
            }
            return this
        }

        override fun addOnSelect(chose: OnSelect): GPSExplain {
            return this
        }

        override fun addOnSelectCancel(cancel: OnSelectOk): Notice {
            dialog?.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                context.getString(android.R.string.cancel)
            ) { d, _ ->
                d.cancel()
                cancel.invoke()
            }
            return this
        }

        override fun addOnSelectOk(ok: OnSelectOk): Notice {
            dialog?.setButton(
                DialogInterface.BUTTON_POSITIVE,
                context.getString(android.R.string.ok)
            ) { d, _ ->
                d.cancel()
                ok.invoke()
            }
            return this
        }

        override val isShowing: Boolean
            get() = dialog?.isShowing ?: false
    }
}