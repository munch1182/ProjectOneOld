package com.munch.project.testsimple.queue

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import com.munch.lib.helper.AddRemoveSetHelper
import com.munch.lib.helper.ServiceBindHelper
import com.munch.lib.log
import java.util.concurrent.Executors

/**
 * 带队列的服务
 * 主要特点是防堵塞
 * 应用于高频率的操作，比如蓝牙通信，可以避免同一时间请求过多造成的拥堵和丢失
 *
 * Create by munch1182 on 2020/12/22 15:42.
 */
class QueueService : Service() {

    private lateinit var looper: Looper
    private lateinit var serviceHandler: Handler

    companion object {

        fun start(context: Context) {
            context.startService(intent(context))
        }

        fun intent(context: Context): Intent {
            return Intent(context, QueueService::class.java)
        }
    }

    object MSG {
        /**
         * @see [QueueService.uiHandler]
         */
        const val UI_NOTIFY = 1000
        const val UI_IDLE = 1001

        /**
         * @see [ServiceHandler]
         */
        const val SERVICE_NOTIFY = 2000
        const val SERVICE_LONG_TIME_REQUEST = 2001
        const val SERVICE_LONG_TIME_REQUEST_NOW = 2002
    }

    /**
     * 考虑到应用场景，封装协议是必然的，但此处只是封装的外部对[QueueService]的请求
     * 真正请求应该在[Request2Other]而且应该放在外面
     */
    object RequestService {

        fun sendMsgTest(service: QueueService) {
            service.sendMsgTest(MSG.SERVICE_LONG_TIME_REQUEST)
        }

        fun sendMsgTestNow(service: QueueService) {
            service.sendMsgTest(MSG.SERVICE_LONG_TIME_REQUEST_NOW, obj = "now", now = true)
        }

        fun sendUiNotifyTest(service: QueueService, obj: Any?) {
            service.sendMsgTest(MSG.UI_NOTIFY, obj)
        }
    }

    /**
     * 如果请求多可以进行分类分发，避免单页面代码过多
     */
    private class Request2Other(private val service: QueueService) {

        private val executors by lazy { Executors.newCachedThreadPool() }
        fun requestLongTime(obj: Any?) {
            executors.execute {
                RequestService.sendUiNotifyTest(service, obj)
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        val handlerThread = HandlerThread(this::class.simpleName)
        handlerThread.start()

        looper = handlerThread.looper
        serviceHandler = ServiceHandler(looper)

        //此时仍在主线程
        Looper.myQueue().addIdleHandler {
            return@addIdleHandler false
        }
    }

    private fun sendMsgTest(
        what: Int,
        obj: Any? = null,
        delayMillis: Long = 0L,
        now: Boolean = false
    ) = sendMsg(what, obj, delayMillis + 1000L, now)

    /**
     * 理论上来讲，service中的方法都应该是私有的，
     * 只有[RequestService]与外界通信
     * 这样可以有效管理[MSG]
     * 但是是不是过于麻烦了
     *
     * 考虑自定义类放入[Message.obj]来做类型判断，以此来维护[MSG]
     */
    private fun sendMsg(
        what: Int,
        obj: Any? = null,
        delayMillis: Long = 0L,
        now: Boolean = false
    ) {
        //以1开头的用uiHandler，在主线程
        //以2开头的用serviceHandler，在子线程
        when (what / 1000) {
            1 -> sendMsg(uiHandler, what, obj, delayMillis, now)
            2 -> sendMsg(serviceHandler, what, obj, delayMillis, now)
            else -> sendMsg(serviceHandler, what, obj, delayMillis, now)
        }
    }

    private fun sendMsg(
        handler: Handler,
        what: Int,
        obj: Any? = null,
        delayMillis: Long = 0L,
        now: Boolean = false
    ) {
        if (now) {
            handler.sendMessageAtFrontOfQueue(Message.obtain(handler).apply {
                this.what = what
                this.obj = obj
            })
        } else {
            handler.sendMessageDelayed(Message.obtain(handler).apply {
                this.what = what
                this.obj = obj
            }, delayMillis)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        looper.quit()
        UiNotifyManager.INSTANCE.clear()
    }

    override fun onBind(intent: Intent?): IBinder {
        return ServiceBindHelper.newBinder(this)
    }

    class UiNotifyManager private constructor() : AddRemoveSetHelper<NotifyListener>() {

        companion object {
            //默认即线程同步
            val INSTANCE by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { UiNotifyManager() }
        }

        fun updateAll(what: Int, obj: Any?) {
            arrays.forEach { it.update(what, obj) }
        }
    }

    interface NotifyListener {
        fun update(what: Int, obj: Any?)
    }

    inner class ServiceHandler constructor(looper: Looper) : Handler(looper) {

        /**
         * 统一处理必然会造成代码判断体积膨胀
         * 因此要考虑用[Message.what]来分类，用自定义类放入[Message.obj]来细分场景
         */
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            log("handle")
            when (msg.what) {
                MSG.SERVICE_NOTIFY -> notifyService()
                MSG.SERVICE_LONG_TIME_REQUEST_NOW,
                MSG.SERVICE_LONG_TIME_REQUEST ->
                    Request2Other(this@QueueService).requestLongTime(msg.obj)
            }
        }
    }

    private val uiHandler by lazy {
        Handler(Looper.getMainLooper(),
            Handler.Callback {
                UiNotifyManager.INSTANCE.updateAll(it.what, it.obj)
                return@Callback true
            })
    }

    private fun notifyService() {

    }

}