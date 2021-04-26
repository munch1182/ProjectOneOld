package com.munch.test.project.one.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.bluetooth.*
import com.munch.pre.lib.extend.digitsInput
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

            btDeviceEtSend.digitsInput("0123456789abcdefABCDEF, []x")
            device = btDevice

        }
        model.init(btDevice)
        model.getState().observeOnChanged(this) {
        }

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

        private val state = MutableLiveData(1)
        fun getState() = state.toLiveData()
        private var dev: BtDevice? = null

        fun init(device: BtDevice?) {
            dev = device
           /* val currentDev = BluetoothHelper.INSTANCE.getCurrent()
            if (currentDev.device != null && currentDev.device == dev) {
                updateState(currentDev.state)
            }*/
        }

        fun connectOrDis() {
            if (!BluetoothHelper.INSTANCE.isOpen()) {
                BluetoothHelper.INSTANCE.open()
                return
            }
            val stateVal = state.value!!
            /*if (ConnectState.isConnected(stateVal)) {
                BluetoothHelper.INSTANCE.disconnect()
            } else if (ConnectState.unConnected(stateVal)) {
                val device = dev ?: return
                BluetoothHelper.INSTANCE.connect(device)
            }*/
        }

        fun updateState(newState: Int) {
            state.postValue(newState)
        }

    }
}