package com.munch.lib.weight

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * Create by munch1182 on 2021/8/12 17:47.
 */

interface CornerViewGroup {


    fun set() {
        /*if (this is View) {
            this.background = ViewHelper.newCornerDrawable()
        }*/
    }
}

class CornerLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : LinearLayoutCompat(context, attrs, styleDef), CornerViewGroup

class CornerFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : FrameLayout(context, attrs, styleDef), CornerViewGroup

class CornerConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : ConstraintLayout(context, attrs, styleDef), CornerViewGroup