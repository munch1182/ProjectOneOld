package com.munch.project.one.timer

import android.os.Handler
import android.os.Looper

/**
 * Create by munch1182 on 2022/2/22 16:35.
 */
class HandlerTimer : ITimer {
    private val name = "handler"
    private val record by lazy { Record(name, ExecLog(name)) }

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var stop = false

    override suspend fun add(timer: Timer): Boolean {
        stop = false
        HandlerImp(record.add(timer)).run()
        return true
    }

    override suspend fun del(id: Int): Boolean {
        return false
    }

    override suspend fun query(): MutableList<Timer> {
        return record.queryAll()
    }

    override suspend fun clear(): Boolean {
        stop = true
        handler.removeCallbacks(HandlerImp())
        record.clear()
        return true
    }

    override fun getFile() = record.getFile()

    private inner class HandlerImp(private val timer: Timer? = null) : Runnable {

        override fun run() {
            if (stop) {
                return
            }
            if (timer == null) {
                handler.removeCallbacks(this)
            } else {
                record.exec(timer)
                if (timer.isRepeat) {
                    handler.removeCallbacks(this)
                    handler.postDelayed(this, timer.interval)
                }
            }
        }
    }
}