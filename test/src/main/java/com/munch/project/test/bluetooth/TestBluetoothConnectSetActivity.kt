package com.munch.project.test.bluetooth

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.lifecycleScope
import com.munch.lib.bt.BtConfig
import com.munch.lib.helper.AppHelper
import com.munch.lib.helper.DataStoreHelper
import com.munch.lib.helper.SpHelper
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R
import com.munch.project.test.databinding.TestActivityTestBluetoothConnectSetBinding
import kotlinx.coroutines.launch


/**
 * Create by munch1182 on 2021/3/16 16:58.
 */
class TestBluetoothConnectSetActivity : TestBaseTopActivity() {

    private val bind by bindingTop<TestActivityTestBluetoothConnectSetBinding>(R.layout.test_activity_test_bluetooth_connect_set)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        title = "ConnectSet"
        bind.testBtSure.setOnClickListener {
            putConfig(bind.config ?: BtConfig())
            toast("保存成功")
            AppHelper.hideIm(this)
        }

        lifecycleScope.launch {
            bind.config = getConfigFromDataStore()
        }
    }

    companion object {

        private const val KEY_CONFIG_MAIN = "key_ble_config_main"
        private const val KEY_CONFIG_WRITE = "key_ble_config_write"
        private const val KEY_CONFIG_NOTIFY = "key_ble_config_notify"
        private const val KEY_CONFIG_DESC = "key_ble_config_desc"

        /**
         * 虽然不需要在协程中运行，但是建议还是在协程中运行，因为内部使用了runBlock，会阻塞线程
         */
        fun getConfigFromDataStore(): BtConfig {
            val def = DataStoreHelper.def()
            val main = def.getInBlock(KEY_CONFIG_MAIN, "")
            val write = def.getInBlock(KEY_CONFIG_WRITE, "")
            val notify = def.getInBlock(KEY_CONFIG_NOTIFY, "")
            val desc = def.getInBlock(KEY_CONFIG_DESC, "")
            return BtConfig(main, write, notify, desc)
        }

        fun putConfig(config: BtConfig) {
            DataStoreHelper.def().putInIO(
                hashMapOf(
                    Pair(KEY_CONFIG_MAIN, config.UUID_MAIN_SERVER),
                    Pair(KEY_CONFIG_WRITE, config.UUID_WRITE),
                    Pair(KEY_CONFIG_NOTIFY, config.UUID_NOTIFY),
                    Pair(KEY_CONFIG_DESC, config.UUID_DESCRIPTOR_NOTIFY)
                )
            )
        }


        @JvmStatic
        @BindingAdapter("bindConfigMain")
        fun bindConfigMain(et: EditText, value: BtConfig) {
            if (value.UUID_MAIN_SERVER != et.text.toString()) {
                et.setText(value.UUID_MAIN_SERVER)
                saveConfig(et, value)
            }
        }

        @JvmStatic
        @BindingAdapter("bindConfigWrite")
        fun bindConfigWrite(et: EditText, value: BtConfig) {
            if (value.UUID_WRITE != et.text.toString()) {
                et.setText(value.UUID_WRITE)
                saveConfig(et, value)
            }
        }

        @JvmStatic
        @BindingAdapter("bindConfigNotify")
        fun bindConfigNotify(et: EditText, value: BtConfig) {
            if (value.UUID_NOTIFY != et.text.toString()) {
                et.setText(value.UUID_NOTIFY)
                saveConfig(et, value)
            }
        }

        private fun saveConfig(et: EditText, value: BtConfig) {
            (et.parent as View).tag = value
        }

        @JvmStatic
        @BindingAdapter("bindConfigDesc")
        fun bindConfigDesc(et: EditText, value: BtConfig) {
            if (value.UUID_DESCRIPTOR_NOTIFY != et.text.toString()) {
                et.setText(value.UUID_DESCRIPTOR_NOTIFY)
                saveConfig(et, value)
            }
        }

        @JvmStatic
        @BindingAdapter("updateConfig")
        fun updateConfig(et: EditText, listener: InverseBindingListener?) {
            if (listener != null) {
                et.doAfterTextChanged {
                    listener.onChange()
                }
            }
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "bindConfigMain", event = "updateConfig")
        fun changeConfigMain(et: EditText): BtConfig {
            val config = getConfig(et)
            config.UUID_MAIN_SERVER = et.text.toString()
            saveConfig(et, config)
            return config
        }

        private fun getConfig(et: EditText): BtConfig {
            val tag = (et.parent as View).tag
            return if (tag is BtConfig) tag else BtConfig()

        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "bindConfigWrite", event = "updateConfig")
        fun changeConfigWrite(et: EditText): BtConfig {
            val data = getConfig(et)
            data.UUID_WRITE = et.text.toString()
            saveConfig(et, data)
            return data
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "bindConfigNotify", event = "updateConfig")
        fun changeConfigNotify(et: EditText): BtConfig {
            val data = getConfig(et)
            data.UUID_NOTIFY = et.text.toString()
            saveConfig(et, data)
            return data
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "bindConfigDesc", event = "updateConfig")
        fun changeConfigDesc(et: EditText): BtConfig {
            val data = getConfig(et)
            data.UUID_DESCRIPTOR_NOTIFY = et.text.toString()
            saveConfig(et, data)
            return data
        }
    }
}
