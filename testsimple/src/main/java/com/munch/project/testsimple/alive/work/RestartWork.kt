package com.munch.project.testsimple.alive.work

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import com.munch.project.testsimple.alive.TestDataHelper
import com.munch.project.testsimple.alive.mix.WorkService
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2020/12/15 14:37.
 */
class RestartWork(private val context: Context, parameters: WorkerParameters) :
    Worker(context, parameters) {

    companion object {

        private const val UNIQUE_WORK_NAME = "unique_work_name_for_alive"

        fun work(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
                    PeriodicWorkRequestBuilder<RestartWork>(15, TimeUnit.MINUTES)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .addTag(getTag())
                        .build()
                )
        }

        fun stop(context: Context) {
            WorkManager.getInstance(context)
                .cancelAllWork()
        }

        fun getWork(context: Context): LiveData<MutableList<WorkInfo>> {
            return WorkManager.getInstance(context).getWorkInfosByTagLiveData(getTag())
        }

        fun getTag() = "tag4work"
    }

    override fun doWork(): Result {
        WorkService.start(context)
        TestDataHelper.countWork(context)
        return Result.success()
    }
}