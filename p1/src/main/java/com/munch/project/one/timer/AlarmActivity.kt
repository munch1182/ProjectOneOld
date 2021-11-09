package com.munch.project.one.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.munch.lib.app.AppHelper
import com.munch.lib.base.startServiceInForeground
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.log.AppRuntimeEnvHelper
import com.munch.lib.helper.TimeHelper
import com.munch.lib.helper.service.BaseForegroundService
import com.munch.lib.helper.service.IForegroundService
import com.munch.lib.helper.toDate
import com.munch.lib.log.Log2FileHelper
import com.munch.lib.log.Logger
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityTimerBinding
import java.io.File


/**
 * Create by munch1182 on 2021/10/28 10:48.
 */
class AlarmActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityTimerBinding>()
    private val ah by lazy { AlarmHelper() }

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

//adb -s <dev> shell dumpsys alarm
class AlarmHelper : ITimer {

    companion object {
        private val log2FileHelper = Log2FileHelper(File(AppHelper.app.cacheDir, "alarm"))

        internal val log = Logger("alarm", true).apply {
            setListener { msg, thread ->
                log2FileHelper.write("${System.currentTimeMillis().toDate()}: $msg ($thread)\n")
            }
        }

        internal const val KEY_FLAG = "KEY_ALARM_FLAG"
    }

    private val am by lazy { AppHelper.app.getSystemService(Context.ALARM_SERVICE) as? AlarmManager }

    override fun add(timer: Timer): Boolean {
        am ?: return false
        val service = PendingIntent.getService(
            AppHelper.app, 1028,
            Intent("com.munch.project.one.Alarm").apply { putExtra(KEY_FLAG, 1) },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (!timer.isRepeat) {
            am!!.set(AlarmManager.RTC, timer.time, service)
        } else {
            am!!.setInexactRepeating(AlarmManager.RTC, timer.time, timer.interval, service)
        }
        log.log("设置闹钟：${timer}")
        return true
    }

    override fun del(id: Int): Boolean {
        return false
    }

    override fun query(): MutableList<Timer> {
        return mutableListOf()
    }
}

class TestService :
    BaseForegroundService(IForegroundService.Parameter("test_alarm", "TEST ALARM", 21102)) {

    override fun buildNotification(title: String?): NotificationCompat.Builder {
        return super.buildNotification(title)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(System.currentTimeMillis().toDate())
    }
}

class AlarmService : Service() {
    override fun onCreate() {
        super.onCreate()
        AlarmHelper.log.log("AlarmService onCreate ")
        val sb = StringBuilder()
        AppRuntimeEnvHelper.env.forEach {
            sb.append(it.key).append(":").append(it.value.invoke()).append("\t")
        }
        AlarmHelper.log.log("state:\t$sb")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AlarmHelper.log.log(
            "AlarmService onStartCommand: ${intent?.action}," +
                    "${intent?.extras?.getInt(AlarmHelper.KEY_FLAG)}"
        )
        return super.onStartCommand(intent, flags, startId)
    }
}