package com.munch.test.project.one.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.bluetooth.BluetoothHelper
import com.munch.pre.lib.bluetooth.BtDevice
import com.munch.pre.lib.extend.startActivity
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityBluetoothConnectBinding

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
        bind.apply {
            lifecycleOwner = this@BluetoothConnectActivity
            btDeviceConfig.setOnClickListener {
                startActivity(BluetoothConfigActivity::class.java)
            }

            device = intent?.extras?.getParcelable(KEY_DEVICE) as? BtDevice? ?: return@apply

            btDeviceConnect.setOnClickListener {
                BluetoothHelper.INSTANCE.connect(device)
            }
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

    internal class BluetoothConnectViewModel : ViewModel()
}