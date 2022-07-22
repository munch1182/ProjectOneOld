package com.munch.lib.weight

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

interface FunctionalView

interface DrawableView {

    fun updateDrawable(): Drawable?
}

interface ITextView {

    fun setTextColor(color: ColorStateList)

    fun setTextColor(@ColorInt color: Int)

}