package com.munch.project.one.bluetooth

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.munch.lib.android.extend.lazy
import com.munch.project.one.databinding.LayoutBluetoothFilterBinding

/**
 * Create by munch1182 on 2022/9/30 9:30.
 */
class BluetoothFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : LinearLayout(context, attrs, styleDef) {

    private val bind by lazy {
        LayoutBluetoothFilterBinding.inflate(
            LayoutInflater.from(context), this
        )
    }

    init {
        orientation = VERTICAL
        bind.btFilterDesc.setOnClickListener { toggleFiltersVisible() }
    }

    private fun toggleFiltersVisible() {
        bind.btFilterFilters.visibility = if (bind.btFilterFilters.isVisible) GONE else VISIBLE
        bind.btFilterDesc.isSelected = bind.btFilterFilters.isVisible
    }

}