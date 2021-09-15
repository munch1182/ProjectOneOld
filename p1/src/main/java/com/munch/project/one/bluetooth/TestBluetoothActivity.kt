package com.munch.project.one.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.base.OnViewIntClickListener
import com.munch.lib.base.ViewHelper
import com.munch.lib.base.toHexStr
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothState
import com.munch.lib.bluetooth.setOnState
import com.munch.lib.fast.base.*
import com.munch.lib.fast.recyclerview.*
import com.munch.lib.log.log
import com.munch.lib.result.ResultHelper
import com.munch.lib.weight.CountView
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityBluetoothBinding
import com.munch.project.one.databinding.ItemBtDevScanBinding

/**
 * Create by munch1182 on 2021/8/24 16:01.
 */
class TestBluetoothActivity : BaseBigTextTitleActivity() {

    companion object {
        const val MENU_TAG_REMOVE_BOND = 0
        const val MENU_TAG_BOND = 1
        const val MENU_TAG_DISCONNECT = 2
        const val MENU_TAG_CONNECT = 3
        const val MENU_TAG_LOCK_DEV = 4
        const val MENU_TAG_MORE_INFO = 5

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

        @JvmStatic
        @BindingAdapter("bind_view_count")
        fun bindViewCount(countView: CountView, count: Int) {
            if (countView.getCount() == count) {
                return
            }
            countView.setCount(count)
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "bind_view_count", event = "update_count")
        fun changeViewCount(countView: CountView): Int {
            return countView.getCount()
        }

        @JvmStatic
        @BindingAdapter("update_count")
        fun updateCount(countView: CountView, listener: InverseBindingListener?) {
            if (listener != null) {
                countView.setCountChangeListener {
                    listener.onChange()
                }
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
                    return oldItem.hashCode() == newItem.hashCode() && oldItem.rssi == newItem.rssi
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
    private val lm by lazy { getSystemService(Context.LOCATION_SERVICE) as? LocationManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //此种方式的双向绑定只会改变值，不会改变对象，因此不会引起vm.config()的更新，需要主动获取最新值
        bind.apply {
            config = vm.config().value
            currentFocus?.clearFocus()

            btRv.layoutManager = LinearLayoutManager(this@TestBluetoothActivity)
            btRv.adapter = simpleAdapter

            btScan.setOnClickListener { onClick() }
            btMoreCb.setOnClickListener { toggleMoreCb() }
            btFilterNameEt.addTextChangedListener { vm.filterIfNeed() }
            btFilterMacEt.addTextChangedListener { vm.filterIfNeed() }

            btTimeoutAdd.setOnClickListener { btTimeoutCv.countAdd() }
            btTimeoutReduce.setOnClickListener { btTimeoutCv.countSub() }
        }

        vm.devs().observe(this) { simpleAdapter.set(it) }
        vm.notice().observe(this) { bind.btNotice.text = it }
        vm.config().observe(this) {
            bind.config = it
            currentFocus?.clearFocus()
        }

        val instance = BluetoothHelper.instance
        showBtnStrByState(instance.state.currentState)

        instance.stateListeners.setOnState(this) {
            runOnUiThread { showBtnStrByState(it) }
            updateState()
        }
        instance.bluetoothStateListeners.set(this) { state, dev ->
            val bd = dev?.let { BtItemDev.from(BluetoothDev.from(it)) }
            //因为系统层的STATE_CONNECTING无法通过方法判断
            if (state == BluetoothAdapter.STATE_CONNECTING) {
                bd?.updateConnecting()
            }
            updateState(bd)
        }
    }

    private fun toggleMoreCb() {
        val isExpand = !(bind.btMoreCb.tag as? Boolean ?: false)
        bind.apply {
            btMoreCb.tag = isExpand
            btMoreOption.visibility = if (isExpand) View.VISIBLE else View.GONE
            btMoreCb.animRotate(isExpand)
        }
    }

    private fun View.animRotate(isExpand: Boolean) {
        val from = if (isExpand) 0f else 180f
        RotateAnimation(from, from + 180, width / 2f, height / 2f).apply {
            fillAfter = true
            duration = 100L
            animation = this
        }
    }

    private fun updateState(dev: BtItemDev? = null) {
        val data = simpleAdapter.data.filterNotNull().map { d ->
            if (dev != null) {
                //通过广播获取的设备没有信号值
                if (d.dev.mac == dev.dev.mac) dev.apply {
                    this.dev.rssi = d.dev.rssi
                    this.dev.scanResult = d.dev.scanResult
                } else d
            } else {
                //新建对象用于diff更新
                BtItemDev.from(d.dev)
            }
        }
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
        currentFocus?.clearFocus()
        val instance = BluetoothHelper.instance
        when {
            instance.state.isClose -> {
                ResultHelper.init(this)
                    .with(BluetoothHelper.openIntent())
                    .start {
                    }
            }
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
                    .contactWith(*BluetoothHelper.permissionsScan())
                    .contactWith({ isGpsOpen() }, Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    .start {
                        if (it) {
                            vm.toggleScan()
                        }
                    }
            }
        }
    }

    private fun isGpsOpen() = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: true

    private fun showDevMenu(dev: BtItemDev) {
        val views = ArrayList<TextView>()
        if (dev.dev.isBond) {
            views.add(newItemTextView("REMOVE BOND", MENU_TAG_REMOVE_BOND))
        } else {
            views.add(newItemTextView("BOND", MENU_TAG_BOND))
        }
        if (dev.dev.isConnectedByHelper) {
            views.add(newItemTextView("DISCONNECT", MENU_TAG_DISCONNECT))
        } else {
            views.add(newItemTextView("CONNECT", MENU_TAG_CONNECT))
        }
        if (bind.config?.name != dev.dev.name && bind.config?.mac != dev.dev.mac) {
            views.add(newItemTextView("LOCK DEV", MENU_TAG_LOCK_DEV))
        }
        if (dev.dev.scanResult != null) {
            views.add(newItemTextView("MORE INFO", MENU_TAG_MORE_INFO))
        }
        val clickListener = object : OnViewIntClickListener {
            override fun onClick(v: View?, intVal: Int) {
                super.onClick(v, intVal)
                when (intVal) {
                    MENU_TAG_BOND -> createBond(dev)
                    MENU_TAG_REMOVE_BOND -> removeBond(dev)
                    MENU_TAG_DISCONNECT -> vm.toggleConnect(dev.dev)
                    MENU_TAG_CONNECT -> vm.toggleConnect(dev.dev)
                    MENU_TAG_LOCK_DEV -> vm.lockDev(dev.dev)
                    MENU_TAG_MORE_INFO -> TestBluetoothScanInfoActivity.start(
                        this@TestBluetoothActivity, dev.dev
                    )
                }
            }
        }
        var dialog: AlertDialog? = null
        val sb = StringBuilder()
        sb.append("connectState:${BluetoothHelper.instance.set.getConnectedState()}\n")
            .append("isBond:${dev.dev.isBond}\n")
            .append("isConnected:${dev.dev.isConnectedBySystem()}\n")
            .append("isConnectedGatt:${dev.dev.isConnectedByGatt()}\n")

        dev.dev.scanResult?.scanRecord?.let { sb.append(it.bytes.toHexStr()) }
        dialog = newMenuDialog(dev.dev.name ?: dev.dev.mac, sb.toString()) {
            views.forEach { v ->
                v.setOnClickListener {
                    clickListener.onClick(it)
                    dialog?.dismiss()
                }
                it.addView(v, ViewHelper.newWWLayoutParams())
            }
        }.show()
    }

    private fun newItemTextView(name: String, tag: Int) =
        newItemTextView(name).apply { this.tag = tag }

    private fun createBond(dev: BtItemDev) {
        val createBond = dev.dev.createBond()
        if (createBond) {
            log("开始绑定")
        } else {
            log("绑定失败")
            toast("绑定失败")
        }
    }

    private fun removeBond(dev: BtItemDev) {
        val removeBond = dev.dev.removeBond()
        if (removeBond == true) {
            log("开始移除绑定")
        } else {
            log("移除绑定失败")
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