package com.munch.project.one.bluetooth

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothType
import com.munch.lib.bluetooth.connect.*
import com.munch.lib.bluetooth.scan.scan
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.recyclerview.SimpleDiffAdapter
import com.munch.lib.result.with
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBtDevScanBinding
import java.util.*

/**
 * Create by munch1182 on 2021/12/4 16:36.
 */
class BluetoothActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityBluetoothBinding>()
    private val scanAdapter by lazy {
        SimpleDiffAdapter<BluetoothDev, ItemBtDevScanBinding>(R.layout.item_bt_dev_scan,

            object : DiffUtil.ItemCallback<BluetoothDev>() {
                override fun areItemsTheSame(
                    oldItem: BluetoothDev,
                    newItem: BluetoothDev
                ): Boolean = oldItem.hashCode() == newItem.hashCode()

                override fun areContentsTheSame(
                    oldItem: BluetoothDev,
                    newItem: BluetoothDev
                ) = oldItem.name == newItem.name
            }) { _, bind, bean -> bind.dev = bean }

    }

    @SuppressLint("InlinedApi", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.btRv.apply {
            layoutManager = LinearLayoutManager(this@BluetoothActivity)
            adapter = scanAdapter
        }
        BluetoothHelper.getInstance(this)
            .setConnectSet(BleConnectSet().apply { onConnectSet = YFWatchSet() })
        bind.btScan.setOnClickListener {
            with(Build.VERSION_CODES.S, android.Manifest.permission.BLUETOOTH_SCAN)
                .requestGrant {
                    scanAdapter.set(null)
                    BluetoothHelper.instance.scan(BluetoothType.BLE) {
                        scanAdapter.add(0, it)
                    }
                }
        }

        val dev2 = BluetoothDev.from(this, "83:86:20:A1:05:20", BluetoothType.BLE)!!
        val dev1 = BluetoothDev.from(this, "D4:BF:DF:26:7C:60", BluetoothType.BLE)!!
        with(Build.VERSION_CODES.S, android.Manifest.permission.BLUETOOTH_CONNECT)
            .requestGrant {
                BluetoothHelper.instance.connect(dev2, object : OnConnectListener {
                    override fun onConnected(dev: BluetoothDev) {
                        /*BluetoothHelper.instance.connect(dev2, object : OnConnectListener {
                            override fun onConnected(dev: BluetoothDev) {
                                val byteArray = intArrayOf(0x89,0x56,0x0E,0x00,0x01,0x00,0x00,0x00,0x23,0x20,0x58,0x9D,0xB5,0x3A)
                                    .map { it.toByte() }.toByteArray()
                                thread {
                                    dev1.send(byteArray)
                                    dev2.send(byteArray)
                                    dev1.send(byteArray)
                                    dev2.send(byteArray)
                                }
                            }
                        })*/
                        val byteArray = intArrayOf(0x89,0x56,0x0E,0x00,0x01,0x00,0x00,0x00,0x23,0x20,0x58,0x9D,0xB5,0x3A)
                            .map { it.toByte() }.toByteArray()
                        dev2.send(byteArray)
                        dev2.send(byteArray)
                        dev2.send(byteArray)
                        dev2.send(byteArray)
                        dev2.send(byteArray)
                        dev2.send(byteArray)
                    }
                })
            }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        BluetoothHelper.getInstance(this).stopScan()
        BluetoothHelper.getInstance(this).disconnect("83:86:20:A1:05:20")
        BluetoothHelper.getInstance(this).disconnect("83:BF:DF:26:7C:60")
    }
}

class YFWatchSet : OnConnectSet {
    override fun onConnectSet(gatt: GattWrapper): ConnectFail? {
        if (!gatt.discoverServices().isSuccess) {
            return ConnectFail.ServiceDiscoveredFail()
        }
        if (gatt.requestMtu(247).obj != 247) {
            return ConnectFail.MtuSetFail
        }
        return null
    }
}