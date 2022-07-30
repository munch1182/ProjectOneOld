package com.munch.project.one.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
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
import com.munch.lib.recyclerview.BaseBindRvAdapter
import com.munch.lib.recyclerview.BindViewHolder
import com.munch.lib.recyclerview.registerViewHolder
import com.munch.lib.result.ExplainContactNotice
import com.munch.lib.result.contact
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBluetoothBinding
import com.munch.project.one.databinding.ItemBluetoothRecordBinding
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
    private val adapter = object : BaseBindRvAdapter<Dev>() {

        init {
            registerViewHolder<ItemBluetoothBinding>(Dev.TYPE_BLE)
                .registerViewHolder<ItemBluetoothRecordBinding>(Dev.TYPE_RECORD)
        }

        override fun onBind(holder: BindViewHolder<ViewBinding>, bean: Dev) {
            when (holder.itemViewType) {
                Dev.TYPE_BLE -> {
                    val ble = (bean as Dev.Ble).ble
                    (holder.bind as ItemBluetoothBinding).apply {
                        btDevMac.text = ble.mac
                        btDevName.text = ble.name?.takeIf { it.isNotEmpty() } ?: "N/A"
                        btDevRssi.text = "${ble.rssi}dbm"
                    }
                }
                Dev.TYPE_RECORD -> {
                    val r = (bean as Dev.Record).record
                    (holder.bind as ItemBluetoothRecordBinding).apply {
                        btRecord.text = r
                    }
                }
            }
        }

    }
    private val instance = BluetoothHelper.instance
    private val barText by lazy { TextView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addRight(barText)

        bind.btRv.apply {
            val lm = LinearLayoutManager(this@BluetoothActivity)
            layoutManager = lm
            addItemDecoration(LinearLineItemDecoration(lm))
            adapter = this@BluetoothActivity.adapter
        }

        val e = Dev.Ble(BluetoothDev("123455"))
        adapter.add(e)
        e.children.forEach { adapter.add(it as Dev.Record) }
        adapter.add(Dev.Ble(BluetoothDev("12345335")))
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

                    dispatcher.gatt?.services?.forEach {
                        it.characteristics.forEach { c ->
                            val b =
                                (c.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY
                            val b1 =
                                (c.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE

                            val b2 =
                                (c.properties and BluetoothGattCharacteristic.PERMISSION_WRITE) == BluetoothGattCharacteristic.PERMISSION_WRITE
                            if (b || b1 || b2) {
                                dispatcher.setCharacteristicNotification(c, true)
                            }
                        }
                    }

                    val hidService =
                        dispatcher.getService(UUID.fromString("00001812-0000-1000-8000-00805f9b34fb"))

                    if (hidService == null) {
                        log(11)
                        c.resume(false)
                        return@runBlocking
                    }
                    val protocolMode =
                        hidService.getCharacteristic(UUID.fromString("00002a4e-0000-1000-8000-00805f9b34fb"))
                    if (protocolMode == null) {
                        log(22)
                        c.resume(false)
                        return@runBlocking
                    }
                    val characteristic = dispatcher.readCharacteristic(protocolMode, timeout)
                    if (characteristic == null) {
                        log(33)
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
                        log(44)
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