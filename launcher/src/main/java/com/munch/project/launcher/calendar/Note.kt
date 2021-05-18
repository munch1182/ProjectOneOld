package com.munch.project.launcher.calendar

import com.munch.pre.lib.calender.Day
import com.munch.pre.lib.extend.formatDate

/**
 * Create by munch1182 on 2021/5/18 17:32.
 */
data class Note(
    val note: String,
    var targetTine: Long,
    var color: Int,
    var isFinished: Boolean = false,
    val createTime: Long = System.currentTimeMillis()
) {

    fun getFinishTimeStr(): String {
        return "${"yyyy/MM/dd".formatDate(createTime)}\n - \n${"yyyy/MM/dd".formatDate(targetTine)} \n 还有${
            Day.from(
                targetTine
            ) - Day.now()
        }天"
    }
}