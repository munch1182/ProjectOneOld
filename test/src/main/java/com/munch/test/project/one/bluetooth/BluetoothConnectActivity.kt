package com.munch.test.project.one.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.bluetooth.*
import com.munch.pre.lib.extend.observeOnChanged
import com.munch.pre.lib.extend.startActivity
import com.munch.pre.lib.extend.toLiveData
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityBluetoothConnectBinding
import com.munch.test.project.one.requestPermission

/**
 * Create by munch1182 on 2021/4/9 14:51.
 */
class BluetoothConnectActivity : BaseTopActivity() {

    companion object {

        private const val KEY_DEVICE = "key_bt_connect_device"

        fun start(context: Context, device: BtDevice) {
            context.startActivity(
                BluetoothConnectActivity::class.java,
                Bundle().apply { putParcelable(KEY_DEVICE, device) })
        }
    }

    private val bind by bind<ActivityBluetoothConnectBinding>(R.layout.activity_bluetooth_connect)
    private val model by get(BluetoothConnectViewModel::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btDevice = intent?.extras?.getParcelable(KEY_DEVICE) as? BtDevice?

        bind.apply {
            lifecycleOwner = this@BluetoothConnectActivity
            btDeviceConfig.setOnClickListener {
                startActivity(BluetoothConfigActivity::class.java)
            }

            btDeviceConnect.setOnClickListener {
                requestPermission(*BluetoothHelper.permissions()) {
                    model.connectOrDis()
                }
            }
            device = btDevice ?: return@apply

        }
        model.init(btDevice)
        model.getState().observeOnChanged(this) {
            val title: String
            val state: String
            when (it) {
                ConnectState.STATE_CONNECTED -> {
                    title = "断开连接"
                    state = "已连接"
                }
                ConnectState.STATE_CONNECTING -> {
                    title = "连接中"
                    state = "连接中"
                }
                ConnectState.STATE_DISCONNECTED -> {
                    title = "连接"
                    state = "未连接"
                }
                ConnectState.STATE_DISCONNECTING -> {
                    title = "断开连接"
                    state = "正在断开"
                }
                else -> throw IllegalStateException("state: $it")
            }
            bind.btDeviceConnect.text = title
            bind.btDeviceState.text = state
        }
        BluetoothHelper.INSTANCE.getStateListeners()
            .setWhenResume(this, object : BtConnectStateListener {
                override fun onStateChange(oldState: Int, newState: Int) {
                    model.updateState(newState)
                }
            })
        BluetoothHelper.INSTANCE.getConnectListeners()
            .setWhenResume(this, object : BtConnectFailListener() {
                @SuppressLint("SetTextI18n")
                override fun connectFail(e: Exception) {
                    runOnUiThread { bind.btDeviceState.text = "连接失败: ${getReason(e)}" }
                }

                private fun getReason(e: Exception): String {
                    return e.message ?: e.cause?.message ?: "null"
                }
            })
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        val config = BluetoothConfigActivity.getConfigFromDb()
        if (config == null) {
            bind.btDeviceConfig.text = "未配置UUID，点击配置"
        } else {
            bind.btDeviceConfig.text = "已配置UUID"
        }
        BluetoothHelper.INSTANCE.setConfig(config)
    }

    internal class BluetoothConnectViewModel : ViewModel() {

        private val state = MutableLiveData(ConnectState.STATE_DISCONNECTED)
        fun getState() = state.toLiveData()
        private var dev: BtDevice? = null

        fun init(device: BtDevice?) {
            dev = device
            val currentDev = BluetoothHelper.INSTANCE.getCurrent()
            if (currentDev.device != null && currentDev.device == dev) {
                updateState(currentDev.state)
            }
        }

        fun connectOrDis() {
            if (!BluetoothHelper.INSTANCE.isOpen()) {
                BluetoothHelper.INSTANCE.open()
                return
            }
            val stateVal = state.value!!
            if (ConnectState.isConnected(stateVal)) {
                BluetoothHelper.INSTANCE.disconnect()
            } else if (ConnectState.unConnected(stateVal)) {
                val device = dev ?: return
                BluetoothHelper.INSTANCE.connect(device)
            }
        }

        fun updateState(newState: Int) {
            state.postValue(newState)
        }

    }
}