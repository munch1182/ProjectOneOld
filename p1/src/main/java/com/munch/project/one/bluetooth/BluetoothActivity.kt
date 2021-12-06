package com.munch.project.one.bluetooth

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothType
import com.munch.lib.bluetooth.scan.scan
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.recyclerview.SimpleDiffAdapter
import com.munch.lib.log.log
import com.munch.lib.result.with
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBtDevScanBinding

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

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.btRv.apply {
            layoutManager = LinearLayoutManager(this@BluetoothActivity)
            adapter = scanAdapter
        }
        bind.btScan.setOnClickListener {
            with(Build.VERSION_CODES.S, android.Manifest.permission.BLUETOOTH_SCAN)
                .requestGrant {
                    scanAdapter.set(null)
                    BluetoothHelper.getInstance(this)
                        .scan(BluetoothType.BLE) {
                            log(it)
                            scanAdapter.add(0, it)
                        }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothHelper.getInstance(this).stopScan()
    }
}