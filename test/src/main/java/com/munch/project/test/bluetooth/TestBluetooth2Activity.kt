package com.munch.project.test.bluetooth

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.base.BaseRootActivity
import com.munch.lib.bt.BluetoothHelper
import com.munch.lib.bt.BtConnectListener
import com.munch.lib.bt.BtDevice
import com.munch.lib.helper.startActivity
import com.munch.lib.log
import com.munch.lib.test.LoadViewHelper
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.TestDialog
import com.munch.project.test.R
import com.munch.project.test.databinding.TestActivityTestBluetooth2Binding

/**
 * Create by munch1182 on 2021/3/16 16:58.
 */
class TestBluetooth2Activity : TestBaseTopActivity(), BaseBt {

    companion object {

        fun start(context: Context, device: BtDevice) {
            TestBluetooth2ViewModel.device = device
            context.startActivity(TestBluetooth2Activity::class.java, Bundle())
        }
    }

    private val bind by bindingTop<TestActivityTestBluetooth2Binding>(R.layout.test_activity_test_bluetooth2)
    private val viewModel by get(TestBluetooth2ViewModel::class.java)
    private val loadViewHelper by lazy { LoadViewHelper(bind.testBtContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@TestBluetooth2Activity
            vm = viewModel
        }
        loadViewHelper.bind(this)
        bind.testBtConnect.setOnClickListener {
            showDialog2Connect(TestBluetooth2ViewModel.device)
        }
    }

    private fun showDialog2Connect(dev: BtDevice) {
        TestDialog.simple(this)
            .setTitle("连接蓝牙")
            .setContent("确定以${dev.type}模式连接${dev.mac}吗?")
            .setConfirmListener {
                connect(dev)
            }
            .show()
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
                            startActivity(TestBluetooth2Activity::class.java)
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

    override fun getContext(): BaseRootActivity {
        return this
    }
}

class TestBluetooth2ViewModel : ViewModel() {

    companion object {
        internal lateinit var device: BtDevice
    }

    private val dev = device
    fun getDev(): LiveData<BtDevice> = MutableLiveData(dev)
}