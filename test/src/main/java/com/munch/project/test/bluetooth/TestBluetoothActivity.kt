package com.munch.project.test.bluetooth

import android.os.Bundle
import android.view.KeyEvent
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.base.BaseRootActivity
import com.munch.lib.bt.*
import com.munch.lib.extend.recyclerview.BaseSimpleBindAdapter
import com.munch.lib.helper.AppHelper
import com.munch.lib.helper.digitsInput
import com.munch.lib.helper.upperInput
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R
import com.munch.project.test.databinding.TestActivityTestBluetoothBinding
import com.munch.project.test.databinding.TestLayoutItemBluetoothBinding
import java.util.*

/**
 * Create by munch1182 on 2021/3/3 11:49.
 */
class TestBluetoothActivity : TestBaseTopActivity(), BaseBt {

    private val model by get(TestBtViewModel::class.java)
    private val bind by bindingTop<TestActivityTestBluetoothBinding>(R.layout.test_activity_test_bluetooth)

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

        bind.testBtTypeRg.setOnCheckedChangeListener { _, checkedId ->
            bind.testBtStick.isChecked = checkedId == R.id.test_bt_type_ble
            bind.testBtStick.isEnabled = checkedId != R.id.test_bt_type_ble
        }

        BluetoothHelper.getInstance().init(this)

        val itemAdapter =
            BaseSimpleBindAdapter<BtDevice, TestLayoutItemBluetoothBinding>(R.layout.test_layout_item_bluetooth)
            { holder, data, _ ->
                holder.binding.bt = data
            }.setOnItemClick { _, _, data, _ ->
                copyData(data)
            }.setOnItemLongClick { _, _, data, _ ->
                next(data)
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

    private fun next(data: BtDevice) {
        TestBluetooth2Activity.start(this, data)
    }

    override fun getContext(): BaseRootActivity {
        return this
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