package com.munch.project.test.bluetooth

import android.os.Bundle
import android.view.KeyEvent
import android.widget.RadioButton
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.bt.*
import com.munch.lib.extend.recyclerview.BaseSimpleBindAdapter
import com.munch.lib.helper.AppHelper
import com.munch.lib.helper.digitsInput
import com.munch.lib.helper.upperInput
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.TestDialog
import com.munch.project.test.LoadViewHelper
import com.munch.project.test.R
import com.munch.project.test.databinding.TestActivityTestBluetoothBinding
import com.munch.project.test.databinding.TestLayoutItemBluetoothBinding
import com.permissionx.guolindev.PermissionX
import java.util.*

/**
 * Create by munch1182 on 2021/3/3 11:49.
 */
class TestBluetoothActivity : TestBaseTopActivity() {

    private val model by get(TestBtViewModel::class.java)
    private val bind by bindingTop<TestActivityTestBluetoothBinding>(R.layout.test_activity_test_bluetooth)
    private val loadViewHelper by lazy { LoadViewHelper(bind.testBtContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@TestBluetoothActivity
            viewModel = model
            testBtTimeoutCv.apply {
                min = 5
                max = 50
            }
            testBtTimeoutReduce.setOnClickListener {
                testBtTimeoutCv.countSub()
                model.countSet(testBtTimeoutCv.curCount.toLong())
            }
            testBtTimeoutAdd.setOnClickListener {
                testBtTimeoutCv.countAdd()
                model.countSet(testBtTimeoutCv.curCount.toLong())
            }
            testBtScan.setOnClickListener {
                testBtContainer.requestFocus()
                AppHelper.hideIm(this@TestBluetoothActivity)
                checkBt {
                    if (model.isScanning().value!!) {
                        model.stopScan()
                    } else {
                        model.startScan()
                    }
                }
            }
            testBtFilterMacEt.apply {
                upperInput()
                digitsInput("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:")
                var isDel = false
                doOnTextChanged { text, _, _, _ ->
                    if (text != null && !isDel) {
                        if (text.length - text.lastIndexOf(':') == 3) {
                            editableText.append(':')
                        }
                    }
                    isDel = false
                }
                setOnKeyListener { _, keyCode, _ ->
                    isDel = keyCode == KeyEvent.KEYCODE_DEL
                    return@setOnKeyListener false
                }
            }
            model.noticeStr().observe(this@TestBluetoothActivity) {
                testBtNotice.text = it
            }
            model.isScanning().observe(this@TestBluetoothActivity) {
                testBtScan.text = if (it) "停止扫描" else "扫描"
            }
        }

        loadViewHelper.bind(this)

        BluetoothHelper.getInstance().init(this)

        val itemAdapter =
            BaseSimpleBindAdapter<BtDevice, TestLayoutItemBluetoothBinding>(R.layout.test_layout_item_bluetooth)
            { holder, data, _ ->
                holder.binding.bt = data
            }.setOnItemClick { _, _, data, _ ->
                copyData(data)
            }.setOnItemLongClick { _, _, data, _ ->
                TestDialog.simple(this)
                    .setTitle("连接蓝牙")
                    .setContent("确定以${findViewById<RadioButton>(bind.testBtTypeRg.checkedRadioButtonId).text}模式连接${data.mac}吗?")
                    .setConfirmListener { connect(data) }
                    .show()
                true
            }
        bind.testBtRv.apply {
            layoutManager = LinearLayoutManager(this@TestBluetoothActivity)
            this.adapter = itemAdapter
        }
        model.getResList().observe(this) {
            itemAdapter.setData(it)
        }
    }

    private fun copyData(data: BtDevice) {
        AppHelper.put2Clip(text = data.mac)
        toast("mac地址已复制到剪切板")
    }

    private fun connect(data: BtDevice) {
        checkBt {
            BluetoothHelper.getInstance().apply {
                if (canConnect()) {
                    connect(data, object : BtConnectListener {
                        override fun onStart(mac: String) {
                            log("onStart")
                            runOnUiThread {
                                loadViewHelper.startLoadingInTime(10000L)
                            }
                        }

                        override fun connectSuccess(mac: String) {
                            log("connectSuccess")
                            toast("连接成功")
                        }

                        override fun connectFail(e: Exception) {
                            log("connectFail")
                            e.printStackTrace()
                            toast("连接失败:${e.message}")
                        }

                        override fun onFinish() {
                            super.onFinish()
                            log("onFinish")
                            runOnUiThread {
                                loadViewHelper.stopLoading()
                            }
                        }
                    })
                } else {
                    toast("当前状态(${connectState()})不能连接")
                }
            }
        }
    }

    private fun checkBt(func: () -> Unit) {
        PermissionX.init(this)
            .permissions(*BluetoothHelper.permissions())
            .request { allGranted, _, _ ->
                if (!allGranted) {
                    toast("拒绝了权限")
                } else {
                    if (BluetoothHelper.getInstance().open()) {
                        func.invoke()
                    } else {
                        toast("蓝牙开启失败")
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothHelper.getInstance().destroy()
    }

    data class ScanParameter(
        var type: BtType = BtType.Classic,
        var filter: ScanFilter? = null,
        var timeout: Long = 12L
    ) {

        var timeoutInt: Int = timeout.toInt()

    }
}