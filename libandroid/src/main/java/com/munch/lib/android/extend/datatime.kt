package com.munch.lib.android.extend

import com.munch.lib.android.helper.InfoHelper.location
import java.text.SimpleDateFormat
import java.util.*

/**
 * 将时间(毫秒值)格式化成字符串
 */
fun Long.fmt(pattern: String = "yyyy/MM/dd HH:mm") = Date(this).fmt(pattern)

/**
 * 将日期格式化成字符串
 */
fun Date.fmt(pattern: String = "yyyy/MM/dd HH:mm") =
    SimpleDateFormat(pattern, location).format(this)