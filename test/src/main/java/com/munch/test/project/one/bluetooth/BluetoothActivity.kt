package com.munch.test.project.one.bluetooth

import android.os.Bundle
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityBluetoothBinding

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