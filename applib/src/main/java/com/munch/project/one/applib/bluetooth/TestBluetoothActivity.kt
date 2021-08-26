package com.munch.project.one.applib.bluetooth

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.base.OnViewIndexClickListener
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothState
import com.munch.lib.bluetooth.setOnState
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.get
import com.munch.lib.fast.base.newMenuDialog
import com.munch.lib.fast.recyclerview.*
import com.munch.lib.log.log
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
        SimpleDiffAdapter<BtItemDev, ItemBtDevScanBinding>(
            R.layout.item_bt_dev_scan, SimpleItemCallback()
        ) { _, bind, dev ->
            bind.dev = dev
        }.apply {
            setOnItemLongClickListener { _, pos, _ ->
                vm.toggleConnect(data[pos]?.dev)
                true
            }
            setOnItemClickListener { _, pos, _ ->
                val dev = data[pos] ?: return@setOnItemClickListener
                showDevMenu(dev)
            }
        }
    }

    private fun showDevMenu(dev: BtItemDev) {
        if (dev.isConnectedBySystem) {
            newMenuDialog(
                dev.dev.name ?: dev.dev.mac,
                arrayOf("取消绑定", "断开连接"),
                object : OnViewIndexClickListener {
                    override fun onClick(v: View?, index: Int) {
                        super.onClick(v, index)
                        log(index)
                    }
                }).show()
        } else if (dev.isBond && !dev.isConnectedByHelper) {
            newMenuDialog(
                dev.dev.name ?: dev.dev.mac,
                arrayOf("取消绑定", "连接"),
                object : OnViewIndexClickListener {
                    override fun onClick(v: View?, index: Int) {
                        super.onClick(v, index)
                        log(index)
                    }
                }).show()
            //被helper连接但没有被系统连接和绑定
        } else if (dev.isConnectedBySystem) {
            newMenuDialog(
                dev.dev.name ?: dev.dev.mac,
                arrayOf("绑定", "断开连接"),
                object : OnViewIndexClickListener {
                    override fun onClick(v: View?, index: Int) {
                        super.onClick(v, index)
                        log(index)
                    }
                }).show()
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

        val instance = BluetoothHelper.instance
        instance.stateListeners.setOnState(this) {
            runOnUiThread {
                val str = when (it) {
                    BluetoothState.IDLE -> "START SCAN"
                    BluetoothState.SCANNING -> "STOP SCAN"
                    BluetoothState.CONNECTING -> "STOP CONNECT"
                    BluetoothState.CONNECTED -> "DISCONNECT"
                    BluetoothState.CLOSE -> "OPEN"
                    else -> throw IllegalStateException()
                }
                bind.btScan.text = str

                if (instance.state.isConnected) {
                    kotlin.run out@{
                        simpleAdapter.data.forEachIndexed { index, btItemDev ->
                            btItemDev ?: return@forEachIndexed
                            if (btItemDev.dev == instance.connectedDev) {
                                val newItem = BtItemDev(btItemDev)
                                newItem.isConnectedByHelper = true
                                simpleAdapter.update(index, newItem)
                                return@out
                            }
                        }
                    }
                } else if (instance.state.isDisconnect) {
                    kotlin.run out@{
                        simpleAdapter.data.forEachIndexed { index, btItemDev ->
                            btItemDev ?: return@forEachIndexed
                            if (btItemDev.isConnectedByHelper) {
                                val newItem = BtItemDev(btItemDev)
                                newItem.isConnectedByHelper = false
                                simpleAdapter.update(index, newItem)
                                return@out
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onClick() {
        ResultHelper.init(this)
            .with(*BluetoothHelper.permissionsScan())
            .requestSimple { vm.toggleScan() }
    }

}