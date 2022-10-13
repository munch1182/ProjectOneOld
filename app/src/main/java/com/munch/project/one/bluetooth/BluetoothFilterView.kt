package com.munch.project.one.bluetooth

import android.content.Context
import android.text.InputFilter.AllCaps
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.munch.lib.android.extend.InputCharFilter
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
            view.performClick()
            clearEditFocus(event)
            false
        }
        bind.btFilterSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                updateDesc(Filter(rssi = -bind.btFilterSeek.progress))
            }
        })
        bind.btFilterName.addTextChangedListener(afterTextChanged = {
            updateDesc(Filter(name = bind.btFilterName.text.toString()))
        })
        bind.btFilterMac.addTextChangedListener(afterTextChanged = {
            updateDesc(Filter(mac = bind.btFilterMac.text.toString()))
        })
        bind.btFilterMac.filters = arrayOf(InputCharFilter("^[a-zA-Z0-9:]"), AllCaps())
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
            if (rssi != 0) {
                if (sb.isNotEmpty()) {
                    sb.append(", ")
                }
                sb.append(rssi).append(" dBm")
            }
            return sb.toString()
        }
    }
}