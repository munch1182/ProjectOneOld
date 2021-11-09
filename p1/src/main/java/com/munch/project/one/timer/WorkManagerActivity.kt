package com.munch.project.one.timer

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.munch.lib.app.AppHelper
import com.munch.lib.base.startServiceInForeground
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.helper.TimeHelper
import com.munch.project.one.databinding.ActivityTimerBinding
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2021/10/28 10:48.
 */
class WorkManagerActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityTimerBinding>()
    private val ah by lazy { WMHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.timerAdd.setOnClickListener {
            val time = System.currentTimeMillis() + 3 * TimeHelper.MILLIS.MINUTE
            ah.add(Timer(0, time, true, AlarmManager.INTERVAL_FIFTEEN_MINUTES))
            toast("set success")
            startServiceInForeground(Intent(this, TestService::class.java))
        }
    }
}

class WMHelper : ITimer {

    override fun add(timer: Timer): Boolean {
        WorkManager.getInstance(AppHelper.app)
            .enqueue(PeriodicWorkRequestBuilder<TestWork>(15,TimeUnit.MINUTES).build())
        return true
    }

    override fun del(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun query(): MutableList<Timer> {
        TODO("Not yet implemented")
    }
}

class TestWork(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {
    override fun doWork(): Result {
        return Result.success()
    }

}