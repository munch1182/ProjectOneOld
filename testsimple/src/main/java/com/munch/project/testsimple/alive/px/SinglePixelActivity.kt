package com.munch.project.testsimple.alive.px

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import com.munch.lib.SingletonHolder
import com.munch.lib.helper.ScreenReceiverHelper
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.alive.AliveHelper
import com.munch.project.testsimple.alive.px.SinglePixelActivity.Helper
import java.lang.ref.WeakReference

/**
 * 当用户将应用挂在后台并锁屏时，开启一个1像素的activity，以提高应用到前台级别，避免先被kill
 *
 * 失效原因：1、有的rom(meizu)app不在前台无法监听屏幕广播
 * 2、android 10后台启动activity有限制，而且即使有前台服务也无法启动
 *@see Helper.start
 *@see <a href="https://developer.android.google.cn/guide/components/activities/background-starts"></a>
 *
 * Create by munch1182 on 2020/12/14 10:45.
 */
class SinglePixelActivity : TestBaseTopActivity() {

    companion object {

        fun start(context: Context) {
            log("SinglePixelActivity.start()")
            val intent = PendingIntent.getActivity(
                context, 0, Intent(
                    context,
                    SinglePixelActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0
            )
            try {
                intent.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
        }

        fun register(context: Context) {
            val instance = Helper.getInstance(context)
            ScreenReceiverHelper(context)
                .addScreenStateListener(object : ScreenReceiverHelper.ScreenStateListener {
                    override fun onScreenOn(context: Context?) {
                        instance.finish()
                    }

                    override fun onScreenOff(context: Context?) {
                        instance.start()
                    }

                    override fun onUserPresent(context: Context?) {
                    }
                })
                .register()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("SinglePixelActivity onCreate")
        window.setGravity(Gravity.START or Gravity.TOP)
        val attr = window.attributes
        attr.x = 0
        attr.y = 0
        attr.height = 1
        attr.width = 1
        window.attributes = attr
        Helper.getInstance(this).setActivity(this)
    }

    override fun onDestroy() {
        log("SinglePixelActivity onDestroy")
        super.onDestroy()
    }

    class Helper private constructor(private val context: Context) {

        //单例
        companion object : SingletonHolder<Helper, Context>(
            SinglePixelActivity::Helper
        )

        private var wr: WeakReference<Activity>? = null

        fun setActivity(activity: Activity) {
            wr = WeakReference(activity)
        }

        /**
         * 从后台启动activity，android10无法成功
         * https://developer.android.google.cn/guide/components/activities/background-starts
         */
        fun start() {
            /*tryMove2Front()*/
            start(
                context
            )
        }

        private fun tryMove2Front() {
            val appAlive = AliveHelper.isAppRunningForeground(context)
            log("SinglePixelActivity.Helper.start()：Foreground：${appAlive}")
            if (!appAlive) {
                AliveHelper.moveApp2Front(context)
                log(
                    "SinglePixelActivity.Helper.start()：Foreground：${AliveHelper.isAppRunningForeground(
                        context
                    )}"
                )
            }
        }

        fun finish() {
            wr?.get()?.finish()
        }
    }
}