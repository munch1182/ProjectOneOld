package com.munch.test.project.one.bluetooth

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import androidx.core.widget.doOnTextChanged
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
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.bluetooth.*
import com.munch.pre.lib.extend.*
import com.munch.pre.lib.helper.AppHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.base.DataHelper
import com.munch.test.project.one.databinding.ActivityBluetoothBinding
import com.munch.test.project.one.databinding.ItemBluetoothBinding
import com.munch.test.project.one.requestPermission
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/4/8 17:14.
 */
class BluetoothActivity : BaseTopActivity() {

    private val bind by bind<ActivityBluetoothBinding>(R.layout.activity_bluetooth)
    private val model by get(BluetoothViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@BluetoothActivity
            vm = model
            btTimeoutAdd.setOnClickListener { btTimeoutCv.countAdd() }
            btTimeoutReduce.setOnClickListener { btTimeoutCv.countSub() }
            btScan.setOnClickListener {
                requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) { model.scan() }
            }
            btRv.layoutManager = LinearLayoutManager(this@BluetoothActivity)

            btFilterMacEt.apply {
                upperInput()
                digitsInput("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
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
                    BluetoothConnectActivity.start(this@BluetoothActivity,bean)
                }
            }
        bind.btRv.adapter = adapter
        model.isScanning().observe(this) { bind.btScan.text = if (it) "停止扫描" else "扫描" }
        model.getNotice().observe(this) { bind.btNotice.text = it }
        model.getResList().observe(this) { adapter.set(it) }

        obOnResume({}, { model.release() })
    }

    @SuppressLint("MissingPermission")
    internal class BluetoothViewModel : ViewModel() {

        companion object {
            const val KEY_FILTER = "key_filter"
        }

        private var saved = DataHelper.DEFAULT.get(KEY_FILTER, BtFilter())
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
            BluetoothHelper.INSTANCE.init(BaseApp.getInstance())
        }

        fun scan() {
            if (!BluetoothHelper.INSTANCE.isOpen()) {
                BluetoothHelper.INSTANCE.open()
            }
            if (scanning.value == true) stopScan() else startScan()
        }

        private fun startScan() {
            scanning.postValue(true)
            val value = getFilter().value ?: return

            BluetoothHelper.INSTANCE.startScan(
                value.getType(),
                value.timeout.toLong() * 1000L,
                mutableListOf(value.filter),
                object : BtScanListener {

                    private var end = false
                    private var time = 0
                    private val noName = value.noName

                    override fun onStart() {
                        end = false
                        list.clear()
                        res.postValue(list)
                        notice.postValue("开始扫描")
                        countDown()
                    }

                    private fun countDown() {
                        viewModelScope.launch {
                            val maxSec = value.timeout + 1
                            flow {
                                for (i in 1..maxSec) {
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

                    override fun onEnd(device: MutableList<BtDevice>) {
                        end = true
                        notice.postValue("已结束，共扫描到${list.size}个设备，历时${time}s")
                        stopScan()
                    }
                }
            )
        }

        private fun stopScan() {
            scanning.postValue(false)
            BluetoothHelper.INSTANCE.stopScan()
        }

        fun release() {
            stopScan()
            DataHelper.DEFAULT.put(KEY_FILTER, saved)
        }
    }

    @Parcelize
    data class BtFilter(
        var isClassic: Boolean = true,
        var filter: ScanFilter = ScanFilter(strict = false),
        var timeout: Int = 20,
        var noName: Boolean = true
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
            if (other is BtFilter) {
                return other.isClassic == isClassic && other.filter == filter && other.timeout == timeout
            }
            return false
        }
    }

    companion object {
        @JvmStatic
        @BindingAdapter("bind_view_count")
        fun bindViewCount(countView: CountView, count: Int) {
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