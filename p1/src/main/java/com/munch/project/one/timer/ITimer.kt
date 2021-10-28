package com.munch.project.one.timer

import com.munch.lib.helper.toDate

/**
 * Create by munch1182 on 2021/10/28 10:49.
 */
interface ITimer {

    fun add(timer: Timer): Boolean

    fun del(id: Int): Boolean

    fun query(): MutableList<Timer>

}

data class Timer(
    var id: Int,
    //触发时间
    var time: Long,
    //是否重复
    var isRepeat: Boolean = false,
    //重复间隔时间
    var interval: Long = 0,
) {
    //是否已执行过
    var executed: Boolean = false

    //已重复次数
    var repeatedCount: Int = 0

    override fun toString(): String {
        return "Timer{id=$id,time=${time.toDate()},isRepeat=$isRepeat,interval=$interval,executed:$executed,repeatedCount=$repeatedCount}"
    }
}