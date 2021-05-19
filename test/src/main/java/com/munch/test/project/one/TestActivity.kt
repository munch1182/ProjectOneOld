package com.munch.test.project.one

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.munch.pre.lib.extend.formatDate
import com.munch.pre.lib.extend.getService
import com.munch.pre.lib.helper.data.SpHelper
import com.munch.test.project.one.base.BaseTestActivity

/**
 * Create by munch1182 on 2021/4/2 10:33.
 */
class TestActivity : BaseTestActivity() {

    override fun testFun0() {
        val manager = getService<AlarmManager>(Context.ALARM_SERVICE) ?: return
        SpHelper.getSp()
            .put("clock start", "yyyyMMdd HHmmss".formatDate(System.currentTimeMillis()))
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            manager,
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 1000L * 60L * 30L,
            PendingIntent.getBroadcast(
                this, 0, Intent(this, Broad::class.java), 0
            )
        )
    }

    class Broad : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            SpHelper.getSp()
                .put("clock receive", "yyyyMMdd HHmmss".formatDate(System.currentTimeMillis()))
        }
    }
}