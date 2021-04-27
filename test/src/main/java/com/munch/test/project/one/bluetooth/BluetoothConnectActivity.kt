package com.munch.test.project.one.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.bluetooth.*
import com.munch.pre.lib.extend.*
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityBluetoothConnectBinding
import com.munch.test.project.one.requestPermission
import java.util.*

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

    private val requestOpen =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

    @SuppressLint("SetTextI18n")
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
                    if (BluetoothHelper.INSTANCE.isOpen()) {
                        model.connectOrDis()
                    } else {
                        requestOpen.launch(BluetoothHelper.openIntent())
                    }
                }
            }

            btDeviceEtSend.digitsInput("0123456789abcdefABCDEF, []x")
            btDeviceEtSend.setOnKeyListener { _, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
                    btDevice?.getConnector()
                        ?.send(btDeviceEtSend.text.trim().toString().toByteArray())
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
            device = btDevice
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
        BluetoothHelper.INSTANCE.apply {
            connectStateListeners.setWhenResume(this@BluetoothConnectActivity,
                object : BtConnectStateListener {

                    override fun onStateChange(oldState: Int, newState: Int) {
                        model.updateState(newState)
                    }
                })
            connectListeners.setWhenResume(this@BluetoothConnectActivity,
                object : BtConnectFailListener {
                    @SuppressLint("SetTextI18n")
                    override fun onConnectFail(device: BtDevice, reason: Int) {
                        val reasonStr = when (reason) {
                            ConnectFailReason.FAIL_FIND_SERVICE -> "发现服务失败"
                            ConnectFailReason.FAIL_REQUEST_MTU -> "设置MTU失败"
                            else -> "连接失败"
                        }
                        runOnUiThread { bind.btDeviceState.text = "连接失败: $reasonStr" }
                    }
                })
            obOnResume({
                val config = BluetoothConfigActivity.getConfigFromDb()
                if (config == null) {
                    bind.btDeviceConfig.text = "未配置UUID，点击配置"
                } else {
                    bind.btDeviceConfig.text = "已配置UUID"
                    model.connect()
                }
                BluetoothHelper.INSTANCE.setConfig(object : BtConfig() {
                    override var mtu: Int = MAX_MTU

                    override fun onDiscoverService(
                        device: BtDevice,
                        gatt: BluetoothGatt,
                        server: MutableList<BluetoothGattService>
                    ): Boolean {
                        if (config == null) {
                            return super.onDiscoverService(device, gatt, server)
                        }
                        val log = BluetoothHelper.logHelper
                        log.log("service-service")
                        val service =
                            gatt.getService(UUID.fromString(config.UUID_MAIN_SERVER))
                                ?: return false
                        log.log("service-write")
                        val write =
                            service.getCharacteristic(UUID.fromString(config.UUID_WRITE))
                                ?: return false
                        log.log("service-notify")
                        val notify =
                            service.getCharacteristic(UUID.fromString(config.UUID_NOTIFY))
                                ?: return false
                        log.log("service-set-notify")
                        if (!gatt.setCharacteristicNotification(notify, true)) {
                            return false
                        }
                        log.log("service-notifyDesc")
                        val notifyDesc =
                            notify.getDescriptor(UUID.fromString(config.UUID_DESCRIPTOR_NOTIFY))
                                ?: return false
                        notifyDesc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        log.log("service-writeDescriptor")
                        if (!gatt.writeDescriptor(notifyDesc)) {
                            return false
                        }
                        log.log("service-success")
                        setNotify(notify)
                        setWrite(write)
                        return true
                    }
                })
            }, {})
        }
    }

    internal class BluetoothConnectViewModel : ViewModel() {

        private val state = MutableLiveData(ConnectState.STATE_DISCONNECTED)
        fun getState() = state.toLiveData()
        private var dev: BtDevice? = null

        fun init(device: BtDevice?) {
            dev = device
            val currentDev = BluetoothHelper.INSTANCE.getCurrent() ?: return
            if (currentDev.device == dev) {
                updateState(currentDev.getState())
            }
        }

        fun connectOrDis() {
            val dev = this.dev ?: return
            if (!BluetoothHelper.INSTANCE.isOpen()) {
                BluetoothHelper.INSTANCE.open()
                return
            }
            val stateVal = state.value!!
            if (ConnectState.isConnected(stateVal)) {
                dev.getConnector().disconnect()
            } else if (ConnectState.unConnected(stateVal)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    dev.getConnector().connect()
                } else {
                    dev.getConnector().connectCompat()
                }
            }
        }

        fun updateState(newState: Int) {
            state.postValue(newState)
        }

        fun connect() {
            val state = BluetoothHelper.INSTANCE.getCurrent()?.getState() ?: this.state.value!!
            this.state.postValue(state)
            if (ConnectState.unConnected(state)) {
                connectOrDis()
            }
        }

    }
}