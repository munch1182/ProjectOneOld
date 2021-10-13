package com.munch.project.one.bluetooth

import android.content.Context
import android.os.Bundle
import com.munch.lib.base.startActivity
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.fast.base.BaseBigTextTitleActivity

/**
 * Create by munch1182 on 2021/8/30 16:50.
 */
class BluetoothScanInfoActivity : BaseBigTextTitleActivity() {

    companion object {

        private const val KEY_SCAN_DEV = "key_scan_dev"

        fun start(context: Context, dev: BluetoothDev) {
            context.startActivity(BluetoothScanInfoActivity::class.java, Bundle().apply {
                putParcelable(KEY_SCAN_DEV, dev)
            })
        }
    }
}