@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

inline fun <T> MutableLiveData<T>.immutable(): LiveData<T> = this
inline fun <T> MutableSharedFlow<T>.immutable(): SharedFlow<T> = this
inline fun <T> MutableStateFlow<T>.immutable(): StateFlow<T> = this

inline val <T> MutableList<T>.new: MutableList<T>
    get() = ArrayList(this)

/**
 * 判断该颜色是否为亮色
 *
 * true为亮色, 否则为暗色
 *
 * 通过判断其亮度是否大于0.5
 */
fun @receiver:ColorInt Int.isLight() = ColorUtils.calculateLuminance(this) > 0.5f

/**
 * @param saturation 更改明度, 值0f-1f, 越小越黑
 */
@ColorInt
fun @receiver:ColorInt Int.colorSaturation(saturation: Float): Int {
    val hsv = FloatArray(3) { 0f }
    Color.colorToHSV(this, hsv)
    hsv[2] = saturation
    return Color.HSVToColor(hsv)
}

/**
 * 返回一个随机颜色, 不带透明度
 */
@ColorInt
fun newRandomColor(): Int {
    val r = java.util.Random()
    return Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256))
}