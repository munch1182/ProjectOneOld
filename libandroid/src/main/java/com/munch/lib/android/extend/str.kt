@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.app.Activity
import android.content.pm.PackageManager
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.munch.lib.android.AppHelper
import java.util.regex.Pattern

interface SealedClassToString {
    override fun toString(): String
}

open class SealedClassToStringByName : SealedClassToString {
    override fun toString(): String {
        return javaClass.simpleName
    }
}

/**
 * 判断权限是否被授予
 */
fun String.isGranted() =
    ContextCompat.checkSelfPermission(AppHelper, this) == PackageManager.PERMISSION_GRANTED

/**
 * 判断被拒绝的权限是否是被永久拒绝
 */
fun String.isDenied(act: Activity) = !ActivityCompat.shouldShowRequestPermissionRationale(act, this)

/**
 * 统计[str]在字符串中出现的次数，如果未出现，则返回0
 *
 * @param str 需要统计的字符串
 */
fun CharSequence.count(str: String): Int {
    val matcher = Pattern.compile(str).matcher(this)
    var count = 0
    while (matcher.find()) count++
    return count
}

/**
 * 从开始位置将剩下的字符串按照数量分割成集合
 *
 * @param count 分割的数量
 * @param start 字符开始位置
 */
fun CharSequence.split(count: Int, start: Int = 0): Array<CharSequence> {
    val len = length - start
    if (len <= 0) {
        return arrayOf()
    } else if (len < count) {
        return arrayOf(this)
    }
    var s = start
    var e: Int
    val size = len / count + if (len % count == 0) 0 else 1
    return Array(size) {
        e = s + kotlin.math.min(count, length - s)
        this.substring(s, e).apply { s = e }
    }
}


//<editor-fold desc="SpannableString">
/**
 * 将[this]中的部分文字[str]的颜色改为[color]
 *
 * 只会更改找到的第一个位置的文字
 *
 * @param ignoreCase 无视大小写
 */
fun CharSequence.color(
    str: String?,
    @ColorInt color: Int,
    ignoreCase: Boolean = true
): CharSequence {
    val target = (if (ignoreCase) str?.lowercase() else str) ?: return this
    val src = if (ignoreCase) this.toString().lowercase() else this
    val start = src.indexOf(target)
    if (start == -1) return this
    return color(color, start, start + target.length)
}

fun CharSequence.color(@ColorInt color: Int, start: Int = 0, end: Int = length): CharSequence {
    if (start >= end) {
        return this
    }
    return SpannableString(this).color(color, start, end)
}

fun CharSequence.size(
    size: Int,
    dip: Boolean = true,
    start: Int = 0,
    end: Int = length
): CharSequence {
    if (start >= end) {
        return this
    }
    return SpannableString(this).size(size, dip, start, end)
}

inline fun SpannableString.color(@ColorInt color: Int, start: Int = 0, end: Int = length) = apply {
    setSpan(ForegroundColorSpan(color), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
}

inline fun SpannableString.size(size: Int, dip: Boolean = true, start: Int = 0, end: Int = length) =
    apply {
        setSpan(AbsoluteSizeSpan(size, dip), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
    }

inline fun SpannableString.size(
    @FloatRange(from = 0.0) multiple: Float, start: Int = 0, end: Int = length
) = apply {
    setSpan(RelativeSizeSpan(multiple), start, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
}
//</editor-fold>