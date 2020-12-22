package com.munch.project.testsimple.queue

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import com.munch.lib.log

/**
 * Create by munch1182 on 2020/12/22 15:42.
 */
class QueueService : Service() {

    private lateinit var looper: Looper
    private lateinit var serviceHandler: Handler

    companion object {

        fun start(context: Context) {
            context.startService(Intent(context, QueueService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        log(123)
        val handlerThread = HandlerThread(this::class.simpleName)
        handlerThread.start()

        looper = handlerThread.looper
        serviceHandler = ServiceHandler(looper)
    }

    fun sendMsgTest(what: Int, obj: Any?, delayMillis: Long, now: Boolean) =
        sendMsg(what, obj, delayMillis + 1000L, now)

    fun sendMsg(what: Int, obj: Any? = null, delayMillis: Long = 0L, now: Boolean = false) {
        if (now) {
            serviceHandler.sendMessageAtFrontOfQueue(Message.obtain(serviceHandler).apply {
                this.what = what
                this.obj = obj
            })
        } else {
            serviceHandler.sendMessageDelayed(Message.obtain(serviceHandler).apply {
                this.what = what
                this.obj = obj
            }, delayMillis)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        looper.quit()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    class ServiceBinder : Binder() {

    }

    class ServiceHandler constructor(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {

            }
        }
    }
}