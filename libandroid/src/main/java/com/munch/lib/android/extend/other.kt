@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
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
 * 返回一个随机颜色, 透明度为[alpha]
 */
@ColorInt
fun newRandomColor(@FloatRange(from = 0.0, to = 1.0) alpha: Float = 1f): Int {
    val r = java.util.Random()
    return Color.argb((255f * alpha).toInt(), r.nextInt(256), r.nextInt(256), r.nextInt(256))
}

fun <VB : ViewBinding> Class<VB>.inflate(
    inflater: LayoutInflater,
    group: ViewGroup? = null,
    boolean: Boolean = false
): VB? {
    return catch {
        getDeclaredMethod(
            "inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
        ).invoke(null, inflater, group, boolean)?.to()
    }
}

fun <VB : ViewBinding> Class<VB>.inflateByMerge(
    inflater: LayoutInflater,
    group: ViewGroup? = null
): VB? {
    return catch {
        getDeclaredMethod(
            "inflate", LayoutInflater::class.java, ViewGroup::class.java
        ).invoke(null, inflater, group)?.to()
    }
}

fun <D : Any> differ(
    content: D.() -> Int,
    same: D.() -> Int = { hashCode() }
): DiffUtil.ItemCallback<D> =
    object : DiffUtil.ItemCallback<D>() {
        override fun areItemsTheSame(oldItem: D, newItem: D): Boolean {
            return same.invoke(oldItem) == same.invoke(newItem)
        }

        override fun areContentsTheSame(oldItem: D, newItem: D): Boolean {
            return content.invoke(oldItem) == content.invoke(newItem)
        }
    }

fun <D : Any> differSame(
    content: (D, D) -> Boolean,
    same: (D, D) -> Boolean
): DiffUtil.ItemCallback<D> =
    object : DiffUtil.ItemCallback<D>() {
        override fun areItemsTheSame(oldItem: D, newItem: D): Boolean {
            return same.invoke(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: D, newItem: D): Boolean {
            return content.invoke(oldItem, newItem)
        }
    }