package com.munch.lib.extend

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import kotlin.random.Random

fun @receiver:ColorInt Int.toColorStr() = "#${Color.red(this).toHexStr().toString(2)}" +
        Color.green(this).toHexStr().toString(2) +
        Color.blue(this).toHexStr().toString(2)

/**
 * @param darker 更改明度, 值0f-1f, 越小越黑
 */
@ColorInt
fun @receiver:ColorInt Int.darker(darker: Float): Int {
    val hsv = FloatArray(3) { 0f }
    Color.colorToHSV(this, hsv)
    hsv[2] = darker
    return Color.HSVToColor(hsv)
}

@ColorInt
fun randomColor(): Int {
    return Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
}

/**
 * 判断该颜色是否为亮色
 *
 * true为亮色, 否则为暗色
 *
 * 通过判断其亮度是否大于0.5
 */
fun @receiver:ColorInt Int.isLight() = ColorUtils.calculateLuminance(this) > 0.5f