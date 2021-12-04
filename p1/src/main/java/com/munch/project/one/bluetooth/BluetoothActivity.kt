package com.munch.project.one.bluetooth

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothType
import com.munch.lib.bluetooth.scan.OnScannerListener
import com.munch.lib.bluetooth.scan.ScanParameter
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.log.log
import com.munch.lib.result.with
import com.munch.project.one.databinding.ActivityBluetoothBinding

/**
 * Create by munch1182 on 2021/12/4 16:36.
 */
class BluetoothActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityBluetoothBinding>()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothHelper.getInstance(this).setScanListener(this, object : OnScannerListener {
            override fun onDeviceScanned(dev: BluetoothDev) {
                super.onDeviceScanned(dev)
                log(dev)
            }
        })
        bind.btScan.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                with(android.Manifest.permission.BLUETOOTH_SCAN)
                    .requestGrant {
                        BluetoothHelper.getInstance(this)
                            .scan(BluetoothType.CLASSIC, ScanParameter.BleScanParameter().apply {
                                //todo 需要验证
                                ignoreNoName = false
                                justFirst = false
                            })
                    }
            } else {
                BluetoothHelper.getInstance(this).scan(BluetoothType.CLASSIC)
            }
        }
    }
}