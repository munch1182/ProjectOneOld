package com.munch.project.one.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.*
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
import com.munch.lib.helper.ThreadHelper
import com.munch.lib.log.log
import com.munch.lib.notice.Notice
import com.munch.lib.notice.OnSelect
import com.munch.lib.notice.OnSelectOk
import com.munch.lib.recyclerview.BindViewHolder
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
import kotlin.random.Random

/**
 * Created by munch1182 on 2022/5/18 21:20.
 */
@SuppressLint("SetTextI18n")
class BluetoothActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by bind<ActivityBluetoothBinding>()
    private val adapter = object : BindRVAdapter<BluetoothDev, ItemBluetoothBinding>(
        differ({ o, n -> o.rssi == n.rssi }, { o, n -> o.mac == n.mac })
    ) {
        override fun onBind(holder: BindViewHolder<ItemBluetoothBinding>, bean: BluetoothDev) {
            holder.bind.apply {
                btDevMac.text = bean.mac
                btDevName.text = bean.name?.takeIf { it.isNotEmpty() } ?: "N/A"
                "${bean.rssi}dbm".also { btDevRssi.text = it }
            }
        }

    }
    private val instance = BluetoothHelper.instance
    private val barText by lazy { TextView(this) }

    private var connect = false
    private var dev: BluetoothDev? = null
    private var hidDev: BluetoothHidDevice? = null

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
                    if (!connect) {
                        checkOrRequest {
                            //instance.scan()
                            lifecycleScope.launch(Dispatchers.IO) {
                                BluetoothDev("83:26:20:A1:05:E1").apply {
                                    addConnectHandler(TheHandler())
                                    find() && connect(connectListener = object :
                                        ConnectListener {
                                        override fun onStart(mac: String) {
                                        }

                                        override fun onConnectSuccess(mac: String) {
                                            active(this@apply)
                                        }

                                        override fun onConnectFail(mac: String, fail: ConnectFail) {
                                        }

                                    })
                                }
                            }
                        }
                    } else {
                        send()
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

    private fun send() {
        hidDev?.sendReport(
            dev?.dev ?: return,
            8,
            byteArrayOf(2, 0, (Random.nextInt(50) + 1).toByte())
        )
        hidDev?.sendReport(dev?.dev, 8, byteArrayOf(0, 0, 0))
    }

    private val value: BluetoothProfile.ServiceListener =
        object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                register(
                    this@BluetoothActivity.dev ?: return,
                    proxy as? BluetoothHidDevice? ?: return
                )
            }

            override fun onServiceDisconnected(profile: Int) {
            }
        }

    private fun active(dev: BluetoothDev) {
        val a = instance.adapter ?: return
        this.dev = dev
        a.getProfileProxy(ctx, value, BluetoothProfile.HID_DEVICE)

    }

    private val sdp = BluetoothHidDeviceAppSdpSettings(
        "HID", "hid test", "test",
        BluetoothHidDevice.SUBCLASS1_COMBO, MOUSE_KEYBOARD_COMBO
    )
    private val qos = BluetoothHidDeviceAppQosSettings(
        BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
        800, 9, 0, 11250, BluetoothHidDeviceAppQosSettings.MAX
    )

    private fun register(dev: BluetoothDev, hidDev: BluetoothHidDevice) {
        log("register")
        hidDev.registerApp(sdp, null, qos, ThreadHelper.newCachePool(), object :
            BluetoothHidDevice.Callback() {
            override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                super.onAppStatusChanged(pluggedDevice, registered)
                log("registered: $registered")
                val states = intArrayOf(
                    BluetoothProfile.STATE_CONNECTING,
                    BluetoothProfile.STATE_CONNECTED,
                    BluetoothProfile.STATE_DISCONNECTED,
                    BluetoothProfile.STATE_DISCONNECTING
                )
                val pairedDevices = hidDev.getDevicesMatchingConnectionStates(states)
                if (hidDev.getConnectionState(dev.dev) == BluetoothProfile.STATE_DISCONNECTED) {
                    log(dev.mac)
                    hidDev.connect(dev.dev)
                } else {
                    val dev2 = pairedDevices.firstOrNull() ?: return
                    log(dev2.address)
                    if (hidDev.getConnectionState(dev2) == BluetoothProfile.STATE_DISCONNECTED) {
                        hidDev.connect(dev2)
                    }
                }
            }

            override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                super.onConnectionStateChanged(device, state)
                log(state)
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        kotlinx.coroutines.delay(1000L)
                        hidDev.sendReport(
                            device ?: return@launch,
                            8,
                            byteArrayOf(2, 0, 56.toByte())
                        )
                        hidDev.sendReport(device, 8, byteArrayOf(0, 0, 0))
                    }
                    this@BluetoothActivity.dev = dev
                    this@BluetoothActivity.hidDev = hidDev
                    connect = true
                }
            }
        })
    }

    override fun onDestroy() {
        super<BaseFastActivity>.onDestroy()
        dev?.dev?.let {
            hidDev?.disconnect(it)
        }
        hidDev?.unregisterApp()
        instance.adapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDev ?: return)
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

    companion object {
        val MOUSE_KEYBOARD_COMBO = byteArrayOf(
            5.toByte(),
            1.toByte(),
            9.toByte(),
            2.toByte(),
            161.toByte(),
            1.toByte(),
            5.toByte(),
            1.toByte(),
            9.toByte(),
            2.toByte(),
            161.toByte(),
            2.toByte(),
            133.toByte(),
            4.toByte(),
            9.toByte(),
            1.toByte(),
            161.toByte(),
            0.toByte(),
            5.toByte(),
            9.toByte(),
            25.toByte(),
            1.toByte(),
            41.toByte(),
            2.toByte(),
            21.toByte(),
            0.toByte(),
            37.toByte(),
            1.toByte(),
            117.toByte(),
            1.toByte(),
            149.toByte(),
            2.toByte(),
            129.toByte(),
            2.toByte(),
            149.toByte(),
            1.toByte(),
            117.toByte(),
            6.toByte(),
            129.toByte(),
            3.toByte(),
            5.toByte(),
            1.toByte(),
            9.toByte(),
            48.toByte(),
            9.toByte(),
            49.toByte(),
            22.toByte(),
            1.toByte(),
            248.toByte(),
            38.toByte(),
            255.toByte(),
            7.toByte(),
            117.toByte(),
            16.toByte(),
            149.toByte(),
            2.toByte(),
            129.toByte(),
            6.toByte(),
            161.toByte(),
            2.toByte(),
            133.toByte(),
            6.toByte(),
            9.toByte(),
            72.toByte(),
            21.toByte(),
            0.toByte(),
            37.toByte(),
            1.toByte(),
            53.toByte(),
            1.toByte(),
            69.toByte(),
            4.toByte(),
            117.toByte(),
            2.toByte(),
            149.toByte(),
            1.toByte(),
            177.toByte(),
            2.toByte(),
            133.toByte(),
            4.toByte(),
            9.toByte(),
            56.toByte(),
            21.toByte(),
            129.toByte(),
            37.toByte(),
            127.toByte(),
            53.toByte(),
            0.toByte(),
            69.toByte(),
            0.toByte(),
            117.toByte(),
            8.toByte(),
            149.toByte(),
            1.toByte(),
            129.toByte(),
            6.toByte(),
            192.toByte(),
            161.toByte(),
            2.toByte(),
            133.toByte(),
            6.toByte(),
            9.toByte(),
            72.toByte(),
            21.toByte(),
            0.toByte(),
            37.toByte(),
            1.toByte(),
            53.toByte(),
            1.toByte(),
            69.toByte(),
            4.toByte(),
            117.toByte(),
            2.toByte(),
            149.toByte(),
            1.toByte(),
            177.toByte(),
            2.toByte(),
            53.toByte(),
            0.toByte(),
            69.toByte(),
            0.toByte(),
            117.toByte(),
            4.toByte(),
            177.toByte(),
            3.toByte(),
            133.toByte(),
            4.toByte(),
            5.toByte(),
            12.toByte(),
            10.toByte(),
            56.toByte(),
            2.toByte(),
            21.toByte(),
            129.toByte(),
            37.toByte(),
            127.toByte(),
            117.toByte(),
            8.toByte(),
            149.toByte(),
            1.toByte(),
            129.toByte(),
            6.toByte(),
            192.toByte(),
            192.toByte(),
            192.toByte(),
            192.toByte(),
            5.toByte(),
            1.toByte(),
            9.toByte(),
            6.toByte(),
            161.toByte(),
            1.toByte(),
            133.toByte(),
            8.toByte(),
            5.toByte(),
            7.toByte(),
            25.toByte(),
            224.toByte(),
            41.toByte(),
            231.toByte(),
            21.toByte(),
            0.toByte(),
            37.toByte(),
            1.toByte(),
            117.toByte(),
            1.toByte(),
            149.toByte(),
            8.toByte(),
            129.toByte(),
            2.toByte(),
            149.toByte(),
            1.toByte(),
            117.toByte(),
            8.toByte(),
            129.toByte(),
            1.toByte(),
            149.toByte(),
            1.toByte(),
            117.toByte(),
            8.toByte(),
            21.toByte(),
            0.toByte(),
            37.toByte(),
            101.toByte(),
            5.toByte(),
            7.toByte(),
            25.toByte(),
            0.toByte(),
            41.toByte(),
            101.toByte(),
            129.toByte(),
            0.toByte(),
            192.toByte()
        )
    }

}