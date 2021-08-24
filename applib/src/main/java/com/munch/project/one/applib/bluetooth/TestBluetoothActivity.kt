package com.munch.project.one.applib.bluetooth

import android.os.Bundle
import android.view.View
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.get
import com.munch.project.one.applib.R
import com.munch.project.one.applib.databinding.ActivityBluetoothBinding

/**
 * Create by munch1182 on 2021/8/24 16:01.
 */
class TestBluetoothActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityBluetoothBinding>(R.layout.activity_bluetooth)
    private val vm by get(BluetoothViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //此种方式的双向绑定只会改变值，不会改变对象，因此不会引起vm.config()的更新，需要主动获取最新值
        bind.config = vm.config().value
        vm.connectedDevice().observe(this) {
            it ?: return@observe
            bind.btGroup.visibility = View.GONE
            bind.btConnectedDev.text = String.format("当前已连接的设备: %s(%s)", it.name, it.mac)
        }
        bind.btScan.setOnClickListener {
        }

    }

}