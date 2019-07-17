package com.munch.module.bluetooth

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.munch.lib.log.LogLog
import com.munch.lib.test.TestBaseActivity
import com.yscoco.blue.BleManage
import com.yscoco.blue.bean.BlueDevice
import com.yscoco.blue.enums.BleScannerState
import com.yscoco.blue.enums.DeviceState
import com.yscoco.blue.enums.ScanNameType
import com.yscoco.blue.listener.BleScannerListener
import com.yscoco.blue.listener.BleStateListener
import kotlinx.android.synthetic.main.activity_ble_scan_list.*

/**
 * Created by Munch on 2019/7/16 9:31
 */
class BleScanListActivity : TestBaseActivity() {

    private val adapter = object : BaseQuickAdapter<BlueDevice, BaseViewHolder>(R.layout.item_scan_result) {

        override fun convert(helper: BaseViewHolder, item: BlueDevice) {
            val singleDriver = BleManage.getInstance().mySingleDriver
            val state =
                if (singleDriver.connectDevice?.address?.equals(item.device?.address) != true)
                    "未连接" else (if (singleDriver.deviceState == DeviceState.CONNECTING) "连接中" else "已连接")
            helper.setText(R.id.item_tv_name, item.device.name)
                .setText(R.id.item_tv_state, state)
        }
    }
    val scanListener = object : BleScannerListener {
        override fun scanState(state: BleScannerState?) {
            toast("开始扫描")
        }

        override fun scan(device: BlueDevice?) {
            device ?: return
            if (!set.contains(device.device.address)) {
                set.add(device.device.address)
                adapter.addData(device)
            }
        }
    }
    val statelistener = object : BleStateListener {
        override fun reConnected(mac: String?) {
            LogLog.log(mac)
        }

        override fun deviceStateChange(mac: String?, state: DeviceState?) {
            LogLog.log("$mac:$state")
            adapter.notifyDataSetChanged()
        }

        override fun onNotifySuccess(mac: String?) {
            LogLog.log("$mac")
        }
    }
    private val set = HashSet<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_scan_list)

        ble_rv.layoutManager = LinearLayoutManager(this)
        ble_rv.addItemDecoration(RvItemDecoration())
        ble_rv.adapter = adapter
        adapter.setOnItemClickListener { _, _, position ->
            val blueDevice = this.adapter.getItem(position)!!
            val singleDriver = BleManage.getInstance().mySingleDriver
            val state = singleDriver.deviceState
            val device = singleDriver.connectDevice
            if (state == DeviceState.CONNECT) {
                if (device.address != blueDevice.device?.address) {
                    toast("连接" + blueDevice.device.name)
                    singleDriver.connect(blueDevice.device?.address, blueDevice.device, false)
                } else {
                    toast("断开连接")
                    singleDriver.disConnect(false)
                }
            } else if (state == DeviceState.DISCONNECTING) {
                toast("请先等待断开连接完成")
            } else if (state == DeviceState.CONNECTING) {
                toast("连接中")
            } else {
                toast("连接" + blueDevice.device.name)
                singleDriver.connect(blueDevice.device?.address, blueDevice.device, false)
            }
        }

        ble_srl.isEnabled = false
        scan()
    }

    private fun scan() {
        BleManage.getInstance().myBleScannerDriver.apply {
            scan(null, ScanNameType.ALL)
            addBleScannerLister(scanListener)
        }

        BleManage.getInstance().mySingleDriver.addBleStateListener(statelistener)
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManage.getInstance().myBleScannerDriver.removeBleScannerLister(scanListener)
        BleManage.getInstance().mySingleDriver.removeBleStateListener(statelistener)
    }
}