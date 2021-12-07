package com.munch.lib.bluetooth.scan

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothType

/**
 * Create by munch1182 on 2021/12/4 17:22.
 */

class ClassicScanner(
    private val context: Context,
    private val listener: OnScannerListener
) : IScanner {

    private val adapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager?)?.adapter
    private val receiver = BluetoothFoundReceiver()

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    override fun start() {
        super.start()
        receiver.register(context)
        adapter?.startDiscovery()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    override fun stop() {
        super.stop()
        receiver.unregister(context)
        //此方法不会回调BluetoothAdapter.ACTION_DISCOVERY_FINISHED
        adapter?.cancelDiscovery()
        //因此手动调用
        listener.onScanComplete()
    }

    private inner class BluetoothFoundReceiver : BroadcastReceiver() {
        fun register(context: Context) {
            context.registerReceiver(this, IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothDevice.ACTION_FOUND)
            })
        }

        fun unregister(context: Context) {
            context.unregisterReceiver(this)
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            val i = intent ?: return
            when (i.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> listener.onScanStart()
                BluetoothDevice.ACTION_FOUND -> {
                    val bd = i.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return
                    val name = i.getStringExtra(BluetoothDevice.EXTRA_NAME)
                    val rssi = i.getIntExtra(BluetoothDevice.EXTRA_RSSI, 0)
                    val dev = BluetoothDev(name, bd.address, rssi, BluetoothType.CLASSIC, bd)
                    listener.onDeviceScanned(dev)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    BluetoothHelper.logSystem.withEnable { "received action: BluetoothAdapter.ACTION_DISCOVERY_FINISHED." }
                    listener.onScanComplete()
                }
            }
        }
    }
}