package com.munch.project.test.bluetooth

import android.os.Bundle
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.ble.*
import com.munch.lib.extend.recyclerview.BaseSimpleBindAdapter
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R
import com.munch.project.test.databinding.TestActivityTestBluetoothBinding
import com.munch.project.test.databinding.TestLayoutItemBluetoothBinding
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/3/3 11:49.
 */
class TestBluetoothActivity : TestBaseTopActivity() {

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
                checkBt {
                    if (model.isScanning().value!!) {
                        model.stopScan()
                    } else {
                        model.startScan()
                    }
                }
            }
            model.noticeStr().observe(this@TestBluetoothActivity) {
                testBtNotice.text = it
            }
            model.isScanning().observe(this@TestBluetoothActivity) {
                testBtScan.text = if (it) "停止扫描" else "扫描"
            }
        }

        BluetoothHelper.getInstance().init(this)

        val itemAdapter =
            BaseSimpleBindAdapter<BtDeviceBean, TestLayoutItemBluetoothBinding>(R.layout.test_layout_item_bluetooth)
            { holder, data, _ ->
                holder.binding.bt = data
            }
        bind.testBtRv.apply {
            layoutManager = LinearLayoutManager(this@TestBluetoothActivity)
            this.adapter = itemAdapter
        }
        model.getResList().observe(this) {
            itemAdapter.setData(it)
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

    class TestBtViewModel : ViewModel() {
        private val parameterVal = ScanParameter(filter = ScanFilter(strict = false))
        private val parameterData = MutableLiveData(parameterVal)
        fun getParameter(): LiveData<ScanParameter> = parameterData
        private val notice = MutableLiveData("")
        fun noticeStr(): LiveData<String> = notice
        private var startTime: Long = -1L
        private val resList = mutableListOf<BtDeviceBean>()
        private val scanResList = MutableLiveData(resList)
        fun getResList(): LiveData<MutableList<BtDeviceBean>> = scanResList
        private val isScanning = MutableLiveData(false)
        fun isScanning(): LiveData<Boolean> = isScanning
        val scanType = ObservableBoolean(true)

        init {
            scanType.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    if (scanType.get()) {
                        parameterVal.type = BtType.Classic
                    } else {
                        parameterVal.type = BtType.Ble
                    }
                }
            })
        }

        fun countSet(timeout: Long) {
            parameterVal.timeout = timeout
        }

        fun startScan() {
            log(parameterVal)
            resList.clear()
            BluetoothHelper.getInstance().startScan(
                parameterVal.type,
                mutableListOf(parameterVal.filter!!),
                parameterVal.timeout * 1000L, object : BtScanListener {
                    private var end = false
                    private var time = 1L
                    override fun onStart() {
                        startTime = System.currentTimeMillis()
                        resList.clear()
                        isScanning.postValue(true)
                        notice.postValue("开始扫描")
                        countDownTime()
                    }

                    private fun countDownTime() {
                        viewModelScope.launch {
                            flow {
                                for (i in 0 until 100) {
                                    time++
                                    if (end) {
                                        break
                                    }
                                    delay(1000L)
                                    if (end) {
                                        break
                                    }
                                    emit(time)
                                }
                            }.collect {
                                notice.postValue("已扫描${it}s")
                            }
                        }
                    }

                    override fun onScan(device: BtDeviceBean) {
                        log(device)
                        if (!resList.contains(device)) {
                            resList.add(0, device)
                            scanResList.postValue(resList)
                        }
                    }

                    override fun onEnd(device: MutableList<BtDeviceBean>) {
                        end = true
                        isScanning.postValue(false)
                        notice.postValue("扫描结束，有${device.size}个结果，历时${(System.currentTimeMillis() - startTime) / 1000L}s")
                        resList.clear()
                        resList.addAll(device)
                        scanResList.postValue(resList)
                    }
                }
            )
        }

        fun stopScan() {
            BluetoothHelper.getInstance().stopScan()
        }

    }

    data class ScanParameter(
        var type: BtType = BtType.Classic,
        var filter: ScanFilter? = null,
        var timeout: Long = 12L
    ) {

        var timeoutInt: Int = timeout.toInt()

    }
}