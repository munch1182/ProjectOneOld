package com.munch.project.test.bluetooth

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.ble.*
import com.munch.lib.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*

/**
 * Create by munch1182 on 2021/3/3 11:49.
 */
class TestBtViewModel : ViewModel() {
    private val parameterVal =
        TestBluetoothActivity.ScanParameter(filter = ScanFilter(strict = false))
    private val parameterData = MutableLiveData(parameterVal)
    fun getParameter(): LiveData<TestBluetoothActivity.ScanParameter> = parameterData
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
        val filter = parameterVal.filter!!
        if (filter.mac.isNullOrEmpty()) {
            filter.mac = null
        } else {
            filter.mac = filter.mac!!.toUpperCase(Locale.ROOT)
        }
        if (filter.name.isNullOrEmpty()) {
            filter.name = null
        }
        log(parameterVal)
        BluetoothHelper.getInstance().startScan(
            parameterVal.type, mutableListOf(filter),
            parameterVal.timeout * 1000L, object : BtScanListener {
                private var end = false
                private var time = 1L
                override fun onStart() {
                    startTime = System.currentTimeMillis()
                    resList.clear()
                    scanResList.postValue(resList)
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