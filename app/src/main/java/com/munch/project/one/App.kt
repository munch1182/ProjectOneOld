package com.munch.project.one

import androidx.work.*
import com.munch.lib.fast.view.FastApp
import com.munch.project.one.net.BiYingWork
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2022/9/23 16:38.
 */
class App : FastApp() {

    override fun onCreate() {
        super.onCreate()
        thread {
            managerWork()
        }
    }

    private fun managerWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()
        val request =
            PeriodicWorkRequestBuilder<BiYingWork>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30L, TimeUnit.SECONDS)
                .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork( // 使用唯一任务避免启动应用后可能出现堆积的任务多次执行
                "BIYING",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}