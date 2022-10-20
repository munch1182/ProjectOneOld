package com.munch.project.one.bluetooth

import android.content.Context
import android.text.*
import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.munch.lib.android.extend.InputPatternFilter
import com.munch.lib.android.extend.bind
import com.munch.lib.android.extend.hideIme
import com.munch.project.one.databinding.LayoutBluetoothFilterBinding

/**
 * Create by munch1182 on 2022/9/30 9:30.
 */
class BluetoothFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : LinearLayout(context, attrs, styleDef) {

    private val bind by bind<LayoutBluetoothFilterBinding>()
    private val filter = Filter()

    init {
        orientation = VERTICAL
        bind.btFilterDesc.setOnClickListener { toggleFiltersVisible() }

        bind.btFilterSeek.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                view.performClick()
                clearEditFocus(event)
            }
            false
        }
        bind.btFilterSeek.setProgress(100, false)
        bind.btFilterSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                bind.btFilterDesc.ellipsize = TextUtils.TruncateAt.MIDDLE
                updateDesc(Filter(rssi = -bind.btFilterSeek.progress))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        bind.btFilterName.addTextChangedListener(beforeTextChanged = { _, _, _, _ ->
            bind.btFilterDesc.ellipsize = TextUtils.TruncateAt.END
        }, afterTextChanged = { updateDesc(Filter(name = bind.btFilterName.text.toString())) })
        InputMacHelper(bind.btFilterMac).set {
            bind.btFilterDesc.ellipsize = TextUtils.TruncateAt.END
            updateDesc(it)
        }
    }

    private fun updateDesc(filter: Filter) {
        this.filter.update(filter)
        bind.btFilterDesc.text = this.filter.toString()
    }

    private fun toggleFiltersVisible() {
        bind.btFilterFilters.visibility = if (bind.btFilterFilters.isVisible) GONE else VISIBLE
        bind.btFilterDesc.isSelected = bind.btFilterFilters.isVisible
        if (!bind.btFilterFilters.isVisible) {
            hideIme()
        }
    }

    private fun clearEditFocus(event: MotionEvent?) {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            bind.btFilterNameContainer.clearFocus()
            bind.btFilterMacContainer.clearFocus()
        }
    }

    fun hideFilter() {
        if (bind.btFilterFilters.isVisible) {
            toggleFiltersVisible()
        }
    }

    data class Filter(var name: String? = null, var mac: String? = null, var rssi: Int = 0) {

        fun update(filter: Filter) {
            if (filter.name != null) this.name = filter.name
            if (filter.mac != null) this.mac = filter.mac
            if (filter.rssi != 0 && filter.rssi != rssi) this.rssi = filter.rssi
        }

        override fun toString(): String {
            val sb = StringBuilder()
            if (!name.isNullOrBlank()) {
                sb.append(name)
            }
            if (!mac.isNullOrBlank()) {
                if (sb.isNotEmpty()) {
                    sb.append(", ")
                }
                sb.append(mac)
            }
            if (rssi != -100 && rssi != 0) {
                if (sb.isNotEmpty()) {
                    sb.append(", ")
                }
                sb.append(rssi).append(" dBm")
            }
            return sb.toString()
        }
    }

    class InputMacHelper(private val input: EditText) {

        private val maxLen = 17
        private var lastLength = 0

        fun set(update: (Filter) -> Unit) {
            input.addTextChangedListener(
                onTextChanged = { _, _, _, _ ->
                    var mac = input.text.toString()
                    if (mac.length in 3 until maxLen) {
                        if (mac.length % 3 == 0 && mac.last() != ':') { // 当删除到:立即添加会出现这种情况
                            mac = "${mac.substring(0, mac.length - 1)}:${mac.last()}"
                            input.setText(mac)
                            input.setSelection(mac.length)
                        }
                    }
                }, afterTextChanged = {
                    var mac = input.text.toString()
                    val isDel = lastLength > mac.length
                    if (!isDel && mac.length < maxLen) {
                        if (mac.length % 3 == 2) {
                            mac = "${input.text}:"
                            input.setText(mac)
                            input.setSelection(mac.length)
                        }
                    }
                    lastLength = mac.length
                    update.invoke(Filter(mac = mac))
                })
            input.filters = arrayOf(
                LengthFilter(maxLen), InputPatternFilter("[a-zA-Z0-9:]*"), AllCaps()
            )
        }

    }
}