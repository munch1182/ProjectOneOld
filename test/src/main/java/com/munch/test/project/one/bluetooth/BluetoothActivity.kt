package com.munch.test.project.one.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.le.ScanFilter
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.extend.get
import com.munch.lib.fast.weight.CountView
import com.munch.pre.lib.bluetooth.*
import com.munch.pre.lib.extend.*
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.log.log
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.base.DataHelper
import com.munch.test.project.one.base.TestApp
import com.munch.test.project.one.databinding.ActivityBluetoothBinding
import com.munch.test.project.one.databinding.ItemBluetoothBinding
import com.munch.test.project.one.requestPermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Create by munch1182 on 2021/4/8 17:14.
 */
class BluetoothActivity : BaseTopActivity() {

    private val bind by bind<ActivityBluetoothBinding>(R.layout.activity_bluetooth)
    private val model by get(BluetoothViewModel::class.java)

    private val requestOpen =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@BluetoothActivity
            vm = model
            btTimeoutAdd.setOnClickListener { btTimeoutCv.countAdd() }
            btTimeoutReduce.setOnClickListener { btTimeoutCv.countSub() }
            btScan.setOnClickListener {
                if (!BluetoothHelper.INSTANCE.isBtSupport()) {
                    toast("此手机不支持蓝牙")
                    return@setOnClickListener
                }
                if (bind.btTypeBle.isChecked && !BluetoothHelper.INSTANCE.isBleSupport()) {
                    toast("此手机不支持ble")
                    return@setOnClickListener
                }
                val mac = bind.btFilterMacEt.text.toString()
                if (bind.btTypeBle.isChecked && mac.isNotEmpty()
                    && !BluetoothHelper.checkMac(mac)
                ) {
                    toast("请输入正确的mac地址")
                    return@setOnClickListener
                }
                btFilterMacEt.clearFocus()
                if (!BluetoothHelper.INSTANCE.isOpen()) {
                    requestOpen.launch(BluetoothHelper.openIntent())
                    return@setOnClickListener
                }
                requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) {
                    if (it.isSelected) {
                        val bean = BtDevice.from(mac)
                        if (bean == null) {
                            toast("找不到该设备")
                            return@requestPermission
                        }
                        start(bean)
                    } else {
                        model.scan()
                    }
                }
            }
            btRv.layoutManager = LinearLayoutManager(this@BluetoothActivity)

            btFilterMacEt.apply {
                upperInput()
                digitsInput("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:")
                var isDel = false
                doAfterTextChanged { text ->
                    if (text != null && !isDel) {
                        if (text.length == 2 || text.length - text.lastIndexOf(':') == 3) {
                            editableText.append(':')
                        }
                    }
                    isDel = false
                    if (btConnectNow.isChecked) {
                        checkCanConnectNow()
                    }
                }
                setOnKeyListener { _, keyCode, _ ->
                    isDel = keyCode == KeyEvent.KEYCODE_DEL
                    return@setOnKeyListener false
                }
            }

            btConnectNow.setOnClickListener { checkCanConnectNow() }
        }
        val adapter =
            object : BaseBindAdapter<BtDevice, ItemBluetoothBinding>(R.layout.item_bluetooth) {
                override fun onBindViewHolder(
                    holder: BaseBindViewHolder<ItemBluetoothBinding>,
                    bean: BtDevice,
                    pos: Int
                ) {
                    holder.bind.bt = bean
                }
            }.apply {
                setOnItemClickListener { _, bean, _, _ ->
                    AppHelper.put2Clip(this@BluetoothActivity, bean.mac)
                    toast("已复制地址到剪切板")
                }
                setOnItemLongClickListener { _, bean, _, _ ->
                    start(bean)
                }
            }
        bind.btRv.adapter = adapter
        model.isScanning().observeOnChanged(this) {
            bind.btConnectNow.isEnabled = !it
            bind.btScan.text = if (it) "停止扫描" else "扫描"
        }
        model.getNotice().observeOnChanged(this) { bind.btNotice.text = it }
        model.getResList().observeOnChanged(this) { adapter.set(it) }

        obOnResume({ checkCanConnectNow() }, { model.release() })
    }

    private fun start(bean: BtDevice) {
        BluetoothConnectActivity.start(this@BluetoothActivity, bean)
    }

    private fun checkCanConnectNow() {
        bind.apply {
            if (btConnectNow.isChecked) {
                val mac = btFilterMacEt.text.trim().toString()
                if (BluetoothHelper.checkMac(mac)) {
                    btScan.text = "连接"
                    btScan.isSelected = true
                    return@apply
                }
            }
            btScan.text = "扫描"
            btScan.isSelected = false
        }
    }

    @SuppressLint("MissingPermission")
    internal class BluetoothViewModel : ViewModel() {

        companion object {
            const val KEY_FILTER = "key_filter"
        }

        private var saved = DataHelper.DEFAULT.get(KEY_FILTER, BtScanConfig())
        private val filter = MutableLiveData(saved)
        fun getFilter() = filter.toLiveData()
        private val scanning = MutableLiveData(false)
        fun isScanning() = scanning.toLiveData()
        private val notice = MutableLiveData("")
        fun getNotice() = notice.toLiveData()
        private val list = mutableListOf<BtDevice>()
        private val res = MutableLiveData(list)
        fun getResList() = res.toLiveData()

        init {
            val app = TestApp.get()
            if (!app.btInited) {
                BluetoothHelper.INSTANCE.init(app)
                app.btInited = true
            }
            val current = BluetoothHelper.INSTANCE.getCurrent()
            if (current != null) {
                list.add(0, current.device)
                res.postValue(list)
            }
        }

        fun scan() {
            if (!BluetoothHelper.INSTANCE.isOpen()) {
                return
            }
            if (scanning.value == true) stopScan() else startScan()
        }

        private fun startScan() {
            scanning.postValue(true)
            val value = getFilter().value!!

            if (value.name.isNullOrEmpty()) {
                value.name = null
            }
            BluetoothHelper.INSTANCE.startBleScan(
                value.timeout.toLong() * 1000L, mutableListOf(value.toScanFilter()),
                null,
                object : BtScanListener {

                    private var end = false
                    private var time = 0
                    private val noName = value.noName

                    override fun onStart() {
                        log("bluetooth-${list.size}")
                        end = false
                        list.clear()
                        res.postValue(list)
                        notice.postValue("开始扫描")
                        countDown()
                    }

                    private fun countDown() {
                        if (value.timeout <= 2) {
                            return
                        }
                        viewModelScope.launch {
                            val maxSec = value.timeout + 1
                            flow {
                                for (i in 2..maxSec) {
                                    if (end) {
                                        return@flow
                                    }
                                    delay(1000L)
                                    if (end) {
                                        return@flow
                                    }
                                    time = i
                                    emit(i)
                                }
                            }.collect {
                                notice.postValue("已扫描${it}s")
                            }
                        }
                    }

                    override fun onScan(device: BtDevice) {
                        if (noName && device.name.isNullOrEmpty()) {
                            return
                        }
                        list.add(0, device)
                        res.postValue(list)
                    }

                    override fun onEnd(devices: MutableList<BtDevice>) {
                        end = true
                        scanning.postValue(false)
                        notice.postValue("已结束，共扫描到${list.size}个设备，历时${time}s")
                    }

                    override fun onFail(@ScanFailReason errorCode: Int) {
                        log(errorCode)
                    }
                }
            )
        }

        private fun stopScan() {
            BluetoothHelper.INSTANCE.stopScan()
        }

        fun release() {
            stopScan()
            DataHelper.DEFAULT.put(KEY_FILTER, saved)
        }
    }

    @Parcelize
    data class BtScanConfig(
        var isClassic: Boolean = true,
        var name: String? = null,
        var mac: String? = null,
        var timeout: Int = 25,
        var noName: Boolean = true,
        var connectNow: Boolean = true
    ) : Parcelable {

        fun getType(): BtType {
            return if (isClassic) BtType.Classic else BtType.Ble
        }

        override fun hashCode(): Int {
            return super.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other is BtScanConfig) {
                return other.isClassic == isClassic && other.name == name && other.mac == mac && other.timeout == timeout
            }
            return false
        }

        fun toScanFilter(): ScanFilter {
            return ScanFilter.Builder()
                .apply {
                    if (!name.isNullOrEmpty()) {
                        setDeviceName(name)
                    }
                    if (!mac.isNullOrEmpty()) {
                        setDeviceAddress(mac)
                    }
                }.build()
        }
    }

    companion object {
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
}