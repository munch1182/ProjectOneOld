package com.munch.test.project.one.bluetooth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.lifecycleScope
import com.munch.pre.lib.bluetooth.BtConfig
import com.munch.pre.lib.helper.AppHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.base.DataHelper
import com.munch.test.project.one.databinding.ActivityBluetoothConfigBinding
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/4/8 17:57.
 */
class BluetoothConfigActivity : BaseTopActivity() {

    private val bind by bind<ActivityBluetoothConfigBinding>(R.layout.activity_bluetooth_config)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@BluetoothConfigActivity
            lifecycleScope.launch {
                config = getConfigFromDb()
            }
            testBtSure.setOnClickListener {
                putConfig(bind.config)
                toast("保存成功")
                AppHelper.hideIm(this@BluetoothConfigActivity)
            }
        }
    }

    companion object {

        private const val KEY_CONFIG = "key_ble_config"


        /**
         * 虽然不需要在协程中运行，但是建议还是在协程中运行，因为内部使用了runBlock，会阻塞线程
         */
        fun getConfigFromDb(): BtConfig {
            return DataHelper.DEFAULT.get(KEY_CONFIG, BtConfig())
        }

        fun putConfig(config: BtConfig?) {
            DataHelper.DEFAULT.put(KEY_CONFIG, config ?: BtConfig())
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
                et.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        listener.onChange()
                    }
                })
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