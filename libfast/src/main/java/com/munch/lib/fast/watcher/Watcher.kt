package com.munch.lib.fast.watcher

import android.os.*
import android.view.Choreographer
import androidx.annotation.MainThread
import com.munch.lib.log.Logger

/**
 * Create by munch1182 on 2021/4/15 16:49.
 */
object Watcher {

    private const val WATCH_UI_MIN_TIME = 1000L

    private val log = Logger().apply {
        noInfo = true
        noStack = true
        tag = "AppWatcher"
    }

    fun watchMainLoop(): Watcher {
        var time = 0L
        Looper.getMainLooper().setMessageLogging {
            //>>>>> Dispatching to Handler
            time = if (it.startsWith(">")) {
                System.currentTimeMillis()
                //<<<<< Finished to Handler
            } else {
                if (time == 0L) {
                    return@setMessageLogging
                }
                val useTime = System.currentTimeMillis() - time
                if (useTime > WATCH_UI_MIN_TIME) {
                    log.log("${useTime}ms ")
                }
                0L
            }
        }
        return this
    }

    /**
     * 此方法会一直监听fpx，不建议一直开启
     *
     * 此方法不能在子线程中调用
     */
    @MainThread
    fun startFpsMonitor(min: Int = 40): Watcher {
        FpsMonitor.listener {
            if (it < min) {
                log.log("fps: $it")
            }
        }.start()
        return this
    }

    fun stopFpsMonitor() = FpsMonitor.stop()

    /**
     * 严苛模式
     *
     * 输出在Logcat，tag为StrictMode
     */
    fun strictMode(): Watcher {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
        return this
    }


    object FpsMonitor {

        private const val FPS_INTERVAL_TIME = 1000L
        private var isStarted = false
        private var count = 0
        private val handlerThread by lazy { HandlerThread("HANDLER_THREAD_FPS").apply { start() } }
        private val handler by lazy { Handler(handlerThread.looper) }
        private val fpsRunnable by lazy { FpsRunnable() }
        private var listener: ((count: Int) -> Unit)? = null

        fun start() {
            if (!isStarted) {
                isStarted = true
                //1s后重置
                handler.postDelayed(fpsRunnable, FPS_INTERVAL_TIME)
                Choreographer.getInstance().postFrameCallback(fpsRunnable)
            }
        }

        fun listener(listener: (count: Int) -> Unit): FpsMonitor {
            this.listener = listener
            return this
        }

        fun stop() {
            count = 0
            handler.removeCallbacks(fpsRunnable)
            Choreographer.getInstance().removeFrameCallback(fpsRunnable)
            isStarted = false
        }

        class FpsRunnable : Choreographer.FrameCallback, Runnable {

            /**
             * 计算绘制次数
             */
            override fun doFrame(frameTimeNanos: Long) {
                count++
                Choreographer.getInstance().postFrameCallback(this)
            }

            /**
             * 统计结果并重置
             */
            override fun run() {
                listener?.invoke(count)
                count = 0
                handler.postDelayed(this, FPS_INTERVAL_TIME)
            }
        }
    }
}