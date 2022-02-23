package com.munch.project.one.timer

import com.munch.lib.app.AppHelper
import com.munch.lib.helper.data.MMKVHelper
import com.munch.lib.helper.del
import com.munch.lib.helper.toDate
import com.munch.lib.log.Logger
import com.munch.project.one.test.Log2FileByIOHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Create by munch1182 on 2022/2/22 16:59.
 */

class ExecLog(private val name: String) : Logger(tag = "handler", noStack = true) {

    init {
        setListener { msg, thread ->
            val now = System.currentTimeMillis().toDate()
            recordImp.write("$now: $msg(${thread.name})\n")
        }
    }

    private val recordImp by lazy {
        Log2FileByIOHelper(AppHelper.app.cacheDir) { dir -> File(dir, name) }
    }

    fun getFile() = File(AppHelper.app.cacheDir, name)

    fun clear() {
        getFile().del()
    }
}

class Record(private val name: String, private val log: ExecLog) {

    companion object {

        private const val KEY_TIMER = "KEY_TIMER"

        private const val KEY_ID = "KEY_ID"
    }

    private val record by lazy { MMKVHelper(name) }

    fun add(timer: Timer): Timer {
        val id = getId()
        timer.id = id
        record.put(KEY_ID, timer.id)
        record.put(KEY_TIMER + id, timer)
        log.log("put ${KEY_ID + id}=${timer.id}, $KEY_TIMER=$timer")
        return timer
    }

    private fun getId(): Int {
        return (record.get(KEY_ID, 0) ?: 0) + 1
    }

    fun query(id: Int): Timer? {
        return record.get(KEY_TIMER + id, Timer::class.java)
    }

    suspend fun queryAll(): MutableList<Timer> {
        return withContext(Dispatchers.IO) {
            val lastId = getId()
            val list = mutableListOf<Timer>()

            for (i in 0 until lastId) {
                val timer = record.get(KEY_TIMER + i, Timer::class.java) ?: continue
                list.add(timer)
            }
            return@withContext list
        }
    }

    fun del(id: Int) {
        record.remove(KEY_TIMER + id)
        log.log("del timer: $id")
    }

    fun clear() {
        record.clear()
        log.clear()
        log.log("clear")
    }

    fun exec(timer: Timer) {
        timer.executed = true
        timer.repeatedCount++
        record.put(KEY_TIMER + timer.id, timer)
        log.log("exec: $timer")
    }

    fun getFile() = log.getFile()
}