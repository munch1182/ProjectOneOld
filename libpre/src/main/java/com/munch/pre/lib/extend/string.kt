package com.munch.pre.lib.extend

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create by munch1182 on 2021/3/31 16:38.
 */
object StringHelper {

    val LINE_SEPARATOR: String? = System.getProperty("line.separator")
}

fun String.formatDate(date: Date, locale: Locale = Locale.getDefault()): String {
    return try {
        SimpleDateFormat(this, locale).format(date)
    } catch (e: Exception) {
        ""
    }
}

fun String.formatDate(time: Long, locale: Locale = Locale.getDefault()): String {
    return formatDate(Date(time), locale)
}