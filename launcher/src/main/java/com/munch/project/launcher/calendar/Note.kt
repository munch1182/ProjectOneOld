package com.munch.project.launcher.calendar

import android.graphics.Color
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.calender.Day
import com.munch.pre.lib.extend.formatDate
import com.munch.pre.lib.extend.getColorCompat
import com.munch.project.launcher.R
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/5/18 17:32.
 */
data class Note(
    var note: String,
    var targetTime: Long,
    var color: Int,
    var isFinished: Boolean = false,
    var finishedTime: Long = 0,
    val createTime: Long = System.currentTimeMillis(),
) : Comparable<Note> {

    fun getFinishTimeStr(): String {
        return "${"yyyy/MM/dd".formatDate(createTime)}\n - \n${"yyyy/MM/dd".formatDate(targetTime)} \n" +
                "还有${Day.from(targetTime) - Day.now()}天"
    }

    companion object {

        fun test(): Note {
            val now = System.currentTimeMillis()
            return Note(
                Random.nextInt(100).toString(),
                Random.nextLong(now, now + Random.nextLong(5, 10) * 24L * 60L * 60L * 1000L),
                Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)),
                Random.nextBoolean()
            )
        }

        private val colorUnSelect = BaseApp.getInstance().getColorCompat(R.color.colorTextGray)

        fun getColorFinished() = colorUnSelect
    }

    override fun compareTo(other: Note): Int {
        if (isFinished && other.isFinished) {
            return (finishedTime - other.finishedTime).toInt()
        }
        if (isFinished) {
            return 1
        }
        if (other.isFinished) {
            return -1
        }
        return (targetTime - other.targetTime).toInt()
    }

    fun new(): Note {
        return Note(note, targetTime, color, isFinished, finishedTime, createTime)
    }
}