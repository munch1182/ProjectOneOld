package com.munch.project.one.applib.bluetooth

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothState
import com.munch.lib.bluetooth.OnStateChangeListener
import com.munch.lib.bluetooth.set
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.get
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.lib.result.ResultHelper
import com.munch.project.one.applib.R
import com.munch.project.one.applib.databinding.ActivityBluetoothBinding
import com.munch.project.one.applib.databinding.ItemBtDevScanBinding

/**
 * Create by munch1182 on 2021/8/24 16:01.
 */
class TestBluetoothActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityBluetoothBinding>(R.layout.activity_bluetooth)
    private val vm by get(BluetoothViewModel::class.java)
    private val simpleAdapter by lazy {
        SimpleAdapter<BtItemDev, ItemBtDevScanBinding>(R.layout.item_bt_dev_scan) { _, bind, dev ->
            bind.dev = dev
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //此种方式的双向绑定只会改变值，不会改变对象，因此不会引起vm.config()的更新，需要主动获取最新值
        bind.apply {
            config = vm.config().value


            btRv.layoutManager = LinearLayoutManager(this@TestBluetoothActivity)
            btRv.adapter = simpleAdapter

            btScan.setOnClickListener { onClick() }
        }

        vm.devs().observe(this) { simpleAdapter.set(it) }
        vm.notice().observe(this) { bind.btNotice.text = it }

        BluetoothHelper.instance.stateListeners.set(this) {
            val str = when (it) {
                BluetoothState.IDLE -> "START SCAN"
                BluetoothState.SCANNING -> "STOP SCAN"
                BluetoothState.CONNECTING -> "STOP CONNECT"
                BluetoothState.CONNECTED -> "DISCONNECT"
                BluetoothState.CLOSE -> "OPEN"
                else -> throw IllegalStateException()
            }
            bind.btScan.text = str
        }
    }

    private fun onClick() {
        ResultHelper.init(this)
            .with(*BluetoothHelper.permissionsScan())
            .requestSimple { vm.toggleScan() }
    }

}