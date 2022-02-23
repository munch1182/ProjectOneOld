package com.munch.project.one.timer

import android.os.Parcelable
import com.munch.lib.helper.toDate
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * Create by munch1182 on 2021/10/28 10:49.
 */
interface ITimer {

    suspend fun add(timer: Timer): Boolean

    suspend fun del(id: Int): Boolean

    suspend fun query(): MutableList<Timer>

    suspend fun clear(): Boolean

    fun getFile(): File? = null
}

@Parcelize
data class Timer(
    //是否重复
    var isRepeat: Boolean = false,
    //重复间隔时间
    var interval: Long = 0,
    //是否已执行过
    var executed: Boolean = false,
    //设置时间
    var time: Long = System.currentTimeMillis(),
    //已重复次数
    var repeatedCount: Int = 0,
    var id: Int = 0,
) : Parcelable {

    override fun toString(): String {
        return "Timer(isRepeat=$isRepeat, interval=$interval, executed=$executed, time=${time.toDate()}, repeatedCount=$repeatedCount, id=$id)"
    }

    companion object {

        fun id(id: Int) = Timer(id = id)
    }
}