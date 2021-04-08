package com.munch.test.lib.pre.bluetooth

import android.os.Bundle
import com.munch.test.lib.pre.R
import com.munch.test.lib.pre.base.BaseTopActivity
import com.munch.test.lib.pre.databinding.ActivityBluetoothBinding

/**
 * Create by munch1182 on 2021/4/8 17:14.
 */
class BluetoothActivity : BaseTopActivity() {

    private val bind by bind<ActivityBluetoothBinding>(R.layout.activity_bluetooth)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@BluetoothActivity
        }
    }
}