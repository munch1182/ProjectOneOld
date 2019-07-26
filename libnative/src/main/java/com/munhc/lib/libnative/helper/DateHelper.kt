package com.munhc.lib.libnative.helper

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Munch on 2019/7/26 15:04
 */
object DateHelper {

    private val sCalendar = Calendar.getInstance()
    @SuppressLint("ConstantLocale")
    private val sDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun applyPattern(pattern: String): SimpleDateFormat {
        sDateFormat.applyPattern(pattern)
        return sDateFormat
    }

    /**
     * @param type [DateUtils.FORMAT_SHOW_DATE][DateUtils.FORMAT_SHOW_TIME]
     * @see DateUtils.FORMAT_SHOW_DATE
     *
     * @see DateUtils.FORMAT_SHOW_TIME
     */
    fun getDateStr2Now(time: Long, type: Int): CharSequence {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), 0, type)
    }

    /**
     * @param date "yyyy-MM-dd"
     */
    fun getDateStr2NowFromNoTime(date: String, type: Int): CharSequence {
        val strings = date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        sCalendar.set(toInt(strings[0]), toInt(strings[1]), toInt(strings[2]), 0, 0, 0)
        return getDateStr2Now(sCalendar.timeInMillis, type)
    }

    /**
     * @param date "yyyy-MM-dd HH:mm:ss"
     */
    fun getDateStr2NowFromFull(date: String, type: Int): CharSequence {
        val strings =
            date.replace(" ", "-").replace(":", "-").split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        sCalendar.set(
            toInt(strings[0]), toInt(strings[1]), toInt(strings[2]),
            toInt(strings[3]), toInt(strings[4]), toInt(strings[5])
        )
        return getDateStr2Now(sCalendar.timeInMillis, type)
    }

    fun toInt(str: String): Int {
        return Integer.parseInt(str)
    }

    fun format(date: String, oldPattern: String, newPattern: String): CharSequence {
        sDateFormat.applyPattern(oldPattern)
        var parseDate: Date? = null
        try {
            parseDate = sDateFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return if (null != parseDate) {

            applyPattern(newPattern).format(parseDate)
        } else ""
    }
}