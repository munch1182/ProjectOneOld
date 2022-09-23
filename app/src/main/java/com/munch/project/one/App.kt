package com.munch.project.one

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request =
                PeriodicWorkRequestBuilder<BiYingWork>(1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(this).enqueue(request)
        }
    }
}