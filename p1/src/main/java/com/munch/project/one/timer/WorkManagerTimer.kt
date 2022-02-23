package com.munch.project.one.timer

import android.content.Context
import androidx.work.*
import com.munch.lib.app.AppHelper
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2022/2/23 10:39.
 */
class WorkManagerTimer : ITimer {
    companion object {
        internal const val name = "workManager"
        internal const val KEY_ID = "key_id"
    }

    private val record by lazy { Record(name, ExecLog(name)) }
    private val instance by lazy { WorkManager.getInstance(AppHelper.app) }

    private fun Timer.getTag() = "timer${id}"

    override suspend fun add(timer: Timer): Boolean {
        val t = record.add(timer)
        if (t.isRepeat) {
            val request =
                PeriodicWorkRequestBuilder<TestWorker>(t.interval, TimeUnit.MILLISECONDS)
                    .addTag(t.getTag())
                    .setInputData(workDataOf(KEY_ID to t.id))
                    .setConstraints(Constraints.NONE)
                    .build()
            instance.enqueueUniquePeriodicWork(
                t.getTag(),
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        } else {
            val request = OneTimeWorkRequestBuilder<TestWorker>()
                .addTag(t.getTag())
                .setInputData(workDataOf(KEY_ID to t.id))
                .setConstraints(Constraints.NONE)
                .build()
            instance.enqueueUniqueWork(
                t.getTag(),
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
        }
        return true
    }

    override suspend fun del(id: Int): Boolean {
        instance.cancelAllWorkByTag(Timer.id(id).getTag())
        record.del(id)
        return true
    }

    override suspend fun query(): MutableList<Timer> {
        return record.queryAll()
    }

    override suspend fun clear(): Boolean {
        instance.cancelAllWork()
        record.clear()
        return true
    }

    override fun getFile() = record.getFile()
}

class TestWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    private val record by lazy { Record(WorkManagerTimer.name, ExecLog(WorkManagerTimer.name)) }

    override fun doWork(): Result {
        val id = inputData.getInt(WorkManagerTimer.KEY_ID, -1)
        id.takeIf { it != -1 }?.let { record.query(it) }?.let { record.exec(it) }
        return Result.success()
    }
}