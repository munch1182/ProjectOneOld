package com.munch.project.one.applib.bluetooth

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.base.OnViewIntClickListener
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothState
import com.munch.lib.bluetooth.setOnState
import com.munch.lib.fast.base.*
import com.munch.lib.fast.recyclerview.*
import com.munch.lib.result.ResultHelper
import com.munch.project.one.applib.R
import com.munch.project.one.applib.databinding.ActivityBluetoothBinding
import com.munch.project.one.applib.databinding.ItemBtDevScanBinding

/**
 * Create by munch1182 on 2021/8/24 16:01.
 */
class TestBluetoothActivity : BaseBigTextTitleActivity() {

    companion object {
        const val TAG_REMOVE_BOND = 0
        const val TAG_BOND = 1
        const val TAG_DISCONNECT = 2
        const val TAG_CONNECT = 4

        private const val KEY_BT_EXIT_CONNECT = "key_bt_exit_keep_connect"

        var keepConnectWhenExit: Boolean = false
            get() {
                return DataHelper.App.instance.get(KEY_BT_EXIT_CONNECT, false)!!
            }
            set(value) {
                if (field != value) {
                    DataHelper.App.instance.put(KEY_BT_EXIT_CONNECT, value)
                    field = value
                }
            }
    }

    private val bind by bind<ActivityBluetoothBinding>(R.layout.activity_bluetooth)
    private val vm by get(BluetoothViewModel::class.java)
    private val simpleAdapter by lazy {
        SimpleDiffAdapter<BtItemDev, ItemBtDevScanBinding>(
            R.layout.item_bt_dev_scan, object : DiffUtil.ItemCallback<BtItemDev>() {
                override fun areItemsTheSame(oldItem: BtItemDev, newItem: BtItemDev): Boolean {
                    return oldItem.dev == newItem.dev
                }

                override fun areContentsTheSame(oldItem: BtItemDev, newItem: BtItemDev): Boolean {
                    return oldItem.hashCode() == newItem.hashCode()
                }
            }
        ) { _, bind, dev -> bind.dev = dev }.apply {
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
        showBtnStrByState(instance.state.currentState)
        instance.stateListeners.setOnState(this) {
            runOnUiThread { showBtnStrByState(it) }
            updateState()
            //断开连接时延时更新状态
            if (BluetoothHelper.instance.state.isDisconnect) {
                bind.btRv.postDelayed({ updateState() }, 1500L)
            }
        }
    }

    private fun updateState() {
        val data = simpleAdapter.data.filterNotNull().map { dev -> BtItemDev.from(dev.dev) }
        simpleAdapter.set(data.toMutableList())
    }

    private fun showBtnStrByState(state: Int) {
        val str = when (state) {
            BluetoothState.IDLE -> "START SCAN"
            BluetoothState.SCANNING -> "STOP SCAN"
            BluetoothState.CONNECTING -> "STOP CONNECT"
            BluetoothState.CONNECTED -> "DISCONNECT"
            BluetoothState.CLOSE -> "OPEN"
            else -> throw IllegalStateException()
        }
        bind.btScan.text = str
    }

    private fun onClick() {
        val instance = BluetoothHelper.instance
        when {
            instance.state.isConnecting -> {
                ResultHelper.init(this)
                    .with(Manifest.permission.BLUETOOTH)
                    .requestSimple { instance.disconnect() }
            }
            instance.state.isConnected -> {
                ResultHelper.init(this)
                    .with(Manifest.permission.BLUETOOTH)
                    .requestSimple { instance.disconnect() }
            }
            else -> {
                ResultHelper.init(this)
                    .with(*BluetoothHelper.permissionsScan())
                    .requestSimple { vm.toggleScan() }
            }
        }
    }

    private fun showDevMenu(dev: BtItemDev) {
        val views = ArrayList<TextView>()
        if (dev.isBond) {
            views.add(newItemTextView("REMOVE BOND").apply { tag = TAG_REMOVE_BOND })
        } else {
            views.add(newItemTextView("BOND").apply { tag = TAG_BOND })
        }
        if (dev.isConnectedByHelper) {
            views.add(newItemTextView("DISCONNECT").apply { tag = TAG_DISCONNECT })
        } else {
            views.add(newItemTextView("CONNECT").apply { tag = TAG_CONNECT })
        }
        val clickListener = object : OnViewIntClickListener {
            override fun onClick(v: View?, intVal: Int) {
                super.onClick(v, intVal)
                when (intVal) {
                    TAG_BOND -> createBond(dev)
                    TAG_REMOVE_BOND -> removeBond(dev)
                    TAG_DISCONNECT -> vm.toggleConnect(dev.dev)
                    TAG_CONNECT -> vm.toggleConnect(dev.dev)
                }
            }
        }
        var dialog: AlertDialog? = null
        dialog =
            newMenuDialog(
                dev.dev.name ?: dev.dev.mac,
                "isConnected:${dev.dev.isConnectedInSystem()}"
            ) {
                views.forEach { v ->
                    v.setOnClickListener {
                        clickListener.onClick(it)
                        dialog?.dismiss()
                    }
                    it.addView(v)
                }
            }.show()
    }

    private fun createBond(dev: BtItemDev) {
        val createBond = dev.dev.createBond()
        if (createBond) {
            updateState()
        } else {
            toast("绑定成功")
        }
    }

    private fun removeBond(dev: BtItemDev) {
        val removeBond = dev.dev.removeBond()
        if (removeBond == true) {
            updateState()
        } else {
            toast("移除绑定失败")
        }
    }

    override fun showMenu() {
        newMenuBottomDialog({
            it.addView(newCheck { container, name, cb ->
                name.text = "已连接时退出不断开"
                cb.isChecked = keepConnectWhenExit
                container.setOnClickListener {
                    cb.toggle()
                    keepConnectWhenExit = cb.isChecked
                }
            })
        }).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!keepConnectWhenExit || !BluetoothHelper.instance.state.isConnected) {
            BluetoothHelper.instance.destroy()
        }
    }
}