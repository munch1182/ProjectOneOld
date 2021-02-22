@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

import java.text.SimpleDateFormat
import java.util.*

/**
 * Create by munch1182 on 2020/12/10 15:24.
 */

/**
 * 应该用pattern调用
 */
fun String.formatDate(date: Date, timeZone: TimeZone = TimeZone.getDefault()): String {
    return try {
        SimpleDateFormat(this, Locale.getDefault()).apply {
            this.timeZone = timeZone
        }.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun String.formatDate(time: Long) = formatDate(Date(time))