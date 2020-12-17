package com.munch.lib.helper

import android.text.format.DateUtils

/**
 * Create by munch1182 on 2020/12/16 10:47.
 */
object DateHelper {

    /**
     * @param type [DateUtils.FORMAT_SHOW_DATE][DateUtils.FORMAT_SHOW_TIME]
     * @see DateUtils.FORMAT_SHOW_DATE
     *
     * @see DateUtils.FORMAT_SHOW_TIME
     */
    fun getDateStr2Now(time: Long, type: Int): CharSequence {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), 0, type)
    }

}