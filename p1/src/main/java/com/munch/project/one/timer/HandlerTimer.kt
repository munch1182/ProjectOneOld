package com.munch.project.one.timer

import android.os.Handler
import android.os.Looper
import com.munch.lib.helper.data.MMKVHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Create by munch1182 on 2022/2/22 16:35.
 */
class HandlerTimer : ITimer {
    private val record by lazy { Record() }
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val exec by lazy { ExecImp("handler") }

    override suspend fun add(timer: Timer): Boolean {
        record.add(timer)
        HandlerImp(timer).run()
        return true
    }

    override suspend fun del(id: Int): Boolean {
        return false
    }

    override suspend fun query(): MutableList<Timer> {
        return record.queryAll()
    }

    override suspend fun clear(): Boolean {
        record.clear()
        return true
    }

    private inner class HandlerImp(private val timer: Timer) : Runnable {

        override fun run() {
            handler.removeCallbacks(this)
            exec.exec(timer)
            record.exec(timer)
            handler.postDelayed(this, timer.interval)
        }
    }

    private class Record {

        companion object {

            private const val KEY_TIMER = "KEY_TIMER"

            private const val KEY_ID = "KEY_ID"
        }

        private val record by lazy { MMKVHelper("record_handler") }

        fun add(timer: Timer): Timer {
            val id = getId()
            timer.id = id
            record.put(KEY_ID + id, timer.id)
            record.put(KEY_TIMER, timer)
            return timer
        }

        private fun getId(): Int {
            return (record.get(KEY_ID, 0) ?: 0) + 1
        }

        suspend fun queryAll(): MutableList<Timer> {
            val lastId = getId()
            return withContext(Dispatchers.IO) {
                val list = mutableListOf<Timer>()

                val nullTimer: Timer? = null
                for (i in 0..lastId) {
                    val timer = record.get(KEY_ID + i, nullTimer) ?: continue
                    list.add(timer)
                }
                return@withContext list
            }
        }

        fun del(id: Int) {
            record.remove(KEY_ID + id)
        }

        fun clear() {
            record.clear()
        }

        fun exec(timer: Timer) {
            timer.executed = true
            timer.repeatedCount++
            record.put(KEY_ID + timer.id, timer)
        }
    }

}