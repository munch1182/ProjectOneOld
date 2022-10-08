package com.munch.project.one.bluetooth

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.munch.project.one.R

/**
 * Create by munch1182 on 2022/9/30 9:30.
 */
class BluetoothFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : LinearLayout(context, attrs, styleDef) {

    init {
        orientation = VERTICAL
        View.inflate(context, R.layout.layout_bluetooth_filter, this)
    }

}