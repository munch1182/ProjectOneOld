package com.munch.project.one.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.munch.lib.app.AppHelper

/**
 * Create by munch1182 on 2022/2/22 18:02.
 */


class AlarmTimer : ITimer {

    companion object {
        internal const val name = "alarm"
        internal const val KEY_ALARM = "KEY_ALARM"
    }

    private val record by lazy { Record(name, ExecLog(name)) }
    private val am by lazy {
        AppHelper.app.getSystemService(Context.ALARM_SERVICE) as? AlarmManager?
    }

    private fun Timer.getRequestCode() = 1000 + id

    /**
     * [PendingIntent.equals]
     */
    private fun Timer.toPend(): PendingIntent? {
        return PendingIntent.getService(
            AppHelper.app,
            getRequestCode(),
            Intent(AppHelper.app, AlarmService::class.java).apply {
                putExtra(KEY_ALARM, this@toPend.id)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override suspend fun add(timer: Timer): Boolean {
        record.add(timer)
        val pend = timer.toPend() ?: return false
        am?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 1000L,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            pend
        )
        return true
    }

    override suspend fun del(id: Int): Boolean {
        val timer = record.query(id) ?: return false
        am?.cancel(timer.toPend())
        record.del(id)
        return true
    }

    override suspend fun query(): MutableList<Timer> {
        return record.queryAll()
    }

    override suspend fun clear(): Boolean {
        query().forEach { am?.cancel(it.toPend()) }
        record.clear()
        return true
    }

    override fun getFile() = record.getFile()
}

class AlarmService : Service() {
    private val record by lazy { Record(AlarmTimer.name, ExecLog(AlarmTimer.name)) }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getIntExtra(AlarmTimer.KEY_ALARM, -1)?.takeIf { it != -1 }
        id?.let { record.query(it) }?.let { record.exec(it) }
        return super.onStartCommand(intent, flags, startId)
    }
}