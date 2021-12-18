package com.munch.project.one.timer

import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.munch.lib.app.AppHelper
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.helper.toDate
import com.munch.lib.notification.NotificationHelper
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityTimerBinding
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2021/10/28 10:48.
 */
class WorkManagerActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityTimerBinding>()

    companion object {

        const val WORK_NAME = "NOTICE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.timerAdd.setOnClickListener {
            WorkManager.getInstance(AppHelper.app)
                .enqueueUniquePeriodicWork(
                    WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
                    PeriodicWorkRequestBuilder<NotificationWork>(
                        PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                        TimeUnit.MILLISECONDS
                    ).build()
                )
        }
    }
}

class NotificationWork(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    override fun doWork(): Result {
        NotificationHelper.notification(
            1210, "WORK_MANAGER",
            NotificationCompat.Builder(context, "WORK_MANAGER")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(
                    "${System.currentTimeMillis().toDate()}_${Thread.currentThread().name}"
                ).build()
        )
        return Result.success()
    }
}