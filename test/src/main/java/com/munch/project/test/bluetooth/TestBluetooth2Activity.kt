package com.munch.project.test.bluetooth

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.*
import com.munch.lib.base.BaseRootActivity
import com.munch.lib.bt.BluetoothHelper
import com.munch.lib.bt.BtConnectListener
import com.munch.lib.bt.BtConnectStateListener
import com.munch.lib.bt.BtDevice
import com.munch.lib.helper.startActivity
import com.munch.lib.log
import com.munch.lib.test.LoadViewHelper
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.TestDialog
import com.munch.project.test.R
import com.munch.project.test.databinding.TestActivityTestBluetooth2Binding
import kotlinx.coroutines.launch

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
            BluetoothHelper.getInstance().run {
                when {
                    !isOpen() -> BluetoothHelper.getInstance().open()
                    isDisconnected() -> showDialog2Connect(TestBluetooth2ViewModel.device)
                    isConnected() -> BluetoothHelper.getInstance().disConnect()
                    isConnecting() -> {
                    }
                    else -> {
                    }
                }
            }
        }
        bind.testBtConnectSet.setOnClickListener {
            showDialog2ConnectSet()
        }
        BluetoothHelper.getInstance().getBluetoothStateListeners().setWhenResume(this,
            { _, turning, available ->
                if (!turning && available) {
                    showNotice("蓝牙已开启", bind.testBtState)
                } else {
                    showNotice("蓝牙已关闭", bind.testBtState)
                }
                changeBtnStateShow()
            }
        )
        BluetoothHelper.getInstance().getConnectListeners()
            .setWhenResume(this, object : BtConnectListener {
                override fun onStart(mac: String) {
                    showNotice("设备连接中", bind.testBtStateDev)
                }

                override fun connectSuccess(mac: String) {
                    showNotice("设备连接成功", bind.testBtStateDev)
                }

                override fun connectFail(e: Exception) {
                    showNotice("设备连接失败, ${e.message}", bind.testBtStateDev)
                }
            })
        BluetoothHelper.getInstance().getConnectStateListeners()
            .setWhenResume(this, object : BtConnectStateListener {
                override fun onStateChange(oldState: Int, newState: Int) {
                    changeBtnStateShow()
                    if (BluetoothHelper.getInstance().isDisconnected()) {
                        showNotice("设备连接已断开", bind.testBtStateDev)
                    }
                }
            })
    }

    override fun onResume() {
        super.onResume()
        BluetoothHelper.getInstance().run {
            when {
                isConnected() -> {
                    showNotice("设备连接成功", bind.testBtStateDev)
                }
                isConnecting() -> {
                    showNotice("设备连接中", bind.testBtStateDev)
                }
                else -> {
                    showNotice("设备未连接", bind.testBtStateDev)
                }
            }
            if (isOpen()) {
                showNotice("蓝牙已开启", bind.testBtState)
            } else {
                showNotice("蓝牙已关闭", bind.testBtState)
            }
        }
        lifecycleScope.launch {
            val config = TestBluetoothConnectSetActivity.getConfigFromDataStore()
            if (config.UUID_MAIN_SERVER == null) {
                showNotice("UUID未设置", bind.testBtStateSet)
            } else {
                showNotice("UUID已设置", bind.testBtStateSet)
                BluetoothHelper.getInstance().setConfig(config)
            }
        }
        changeBtnStateShow()
    }

    private fun changeBtnStateShow() {
        BluetoothHelper.getInstance().run {
            when {
                !isOpen() -> setBtnText("打开蓝牙")
                isDisconnected() -> setBtnText("连接")
                isConnected() -> setBtnText("断开连接")
                isConnecting() -> setBtnText("连接中")
            }
        }
    }

    private fun setBtnText(text: String) {
        bind.testBtConnect.post {
            bind.testBtConnect.text = text
        }
    }

    private fun showDialog2ConnectSet() {
        startActivity(TestBluetoothConnectSetActivity::class.java)
    }

    private fun showNotice(s: String, text: TextView) {
        text.run { post { text.text = s } }
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

    init {
    }
}
