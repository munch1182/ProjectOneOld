package com.munch.project.one.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.connect.*
import com.munch.lib.bluetooth.scan.OnScannerListener
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.recyclerview.SimpleDiffAdapter
import com.munch.lib.result.contactWith
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
            contactWith(Build.VERSION_CODES.S, android.Manifest.permission.BLUETOOTH_SCAN)
                .contactWith(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .start {
                    if (it) {
                        BluetoothHelper.instance.scanBle(null, object : OnScannerListener {
                            override fun onScanStart() {
                                super.onScanStart()
                                scanAdapter.set(null)
                            }

                            override fun onDeviceScanned(dev: BluetoothDev) {
                                super.onDeviceScanned(dev)
                                scanAdapter.add(0, dev)
                            }
                        })
                    }
                }

        }

        /*val dev2 = BluetoothDev.from(this, "83:86:20:A1:05:20", BluetoothType.BLE)!!
        val dev1 = BluetoothDev.from(this, "D4:BF:DF:26:7C:60", BluetoothType.BLE)!!
        with(Build.VERSION_CODES.S, android.Manifest.permission.BLUETOOTH_CONNECT)
            .requestGrant {
                BluetoothHelper.instance.connect(dev2, object : OnConnectListener {
                    override fun onConnected(dev: BluetoothDev) {
                        *//*BluetoothHelper.instance.connect(dev2, object : OnConnectListener {
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
                        })*//*
                        pool {
                            val byteArray = intArrayOf(0x89,0x56,0x0E,0x00,0x01,0x00,0x00,0x00,0x23,0x20,0x58,0x9D,0xB5,0x3A)
                                .map { it.toByte() }.toByteArray()
                            dev2.send(byteArray)
                            dev2.send(byteArray)
                            dev2.send(byteArray)
                            dev2.send(byteArray)
                            dev2.send(byteArray)
                            dev2.send(byteArray)
                        }
                    }
                })
            }*/
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
        val g = gatt.gatt ?: return ConnectFail.SystemError(1)
        val mainService =
            g.getService(UUID.fromString("461c5198-449c-449b-9fe5-6259dc3fcbed"))
                ?: return ConnectFail.ServiceDiscoveredFail("main")

        val write =
            mainService.getCharacteristic(UUID.fromString("461c0028-449c-449b-9fe5-6259dc3fcbed"))
                ?: return ConnectFail.ServiceDiscoveredFail("write")
        gatt.setWriteCharacteristic(write)
        val notify =
            mainService.getCharacteristic(UUID.fromString("461c0018-449c-449b-9fe5-6259dc3fcbed"))
                ?: return ConnectFail.ServiceDiscoveredFail("notify")
        if (!g.setCharacteristicNotification(notify, true)) {
            return ConnectFail.NotificationSetFail
        }
        val notifyDesc =
            notify.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                ?: return ConnectFail.ServiceDiscoveredFail("notifyDesc")
        notifyDesc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        if (!gatt.writeDescriptor(notifyDesc).isSuccess) {
            return ConnectFail.WriteDescriptorFail
        }
        if (gatt.requestMtu(247).obj != 247) {
            return ConnectFail.MtuSetFail
        }
        return null
    }
}