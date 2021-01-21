package com.munch.lib.helper

import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewTreeObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.munch.lib.helper.TimerHelper.ITimerShow
import java.lang.ref.WeakReference

/**
 * 用于实现显示类控件在短时间调用显示又调用取消时不显示
 * 而在稍长时间内调用显示并调用取消时显示更长的时间
 * 以避免一闪而过或者每次最少都会显示的情形
 *
 * 相关控件都可以桥接实现[ITimerShow]
 *
 * 因为最终调用都在主线程的Handler中，所以[ITimerShow]也必须在主线程中创建，
 * 但[TimerHelper]可以在子线程中创建，并且可以在子线程中调用[show]等方法
 *
 * 注意：lazy会在第一次调用处所在的线程创建
 *
 * 当一个对象只被WeakReference持有时，就会被GC回收
 *
 * Create by munch1182 on 2020/12/25 17:24.
 */
class TimerHelper private constructor(private val owner: LifecycleOwner, timeShow: ITimerShow) {

    class TimerDialog(private var dialog: Dialog?) : ITimerShow {

        init {
            /**
             * 避免内存泄漏，但相应的每次调用必须新创建，即一次性使用
             */
            dialog?.window?.decorView?.viewTreeObserver
                ?.addOnWindowAttachListener(object : ViewTreeObserver.OnWindowAttachListener {
                    override fun onWindowAttached() {
                    }

                    override fun onWindowDetached() {
                        dialog = null
                    }
                })
        }

        override fun show() {
            dialog?.show()
        }

        override fun cancel() {
            dialog?.cancel()
        }
    }

    interface ITimerShow {
        fun show()
        fun cancel()
    }

    companion object {
        const val WHAT_SHOW = 0
        const val WHAT_CANCEL = 1
        const val WHAT_MSG_CANCEL = 2


        private const val TIME_DELAY_DEF = 200L

        fun with(owner: LifecycleOwner, dialog: ITimerShow): TimerHelper {
            return TimerHelper(owner, dialog)
        }

        fun with(owner: LifecycleOwner, dialog: Dialog): TimerHelper {
            return TimerHelper(owner, TimerDialog(dialog))
        }

        fun ITimerShow.withTimer(owner: LifecycleOwner) = with(owner, this)

        fun Dialog.withTimer(owner: LifecycleOwner) = TimerDialog(this).withTimer(owner)
    }

    private val timerShowWeakReference = WeakReference(timeShow)
    private val handler = Handler(Looper.getMainLooper(), Handler.Callback {
        when (it.what) {
            WHAT_MSG_CANCEL -> {
                cancelInMain()
            }
            WHAT_SHOW -> {
                if (showAtTime == null) {
                    timerShowWeakReference.get()?.show() ?: return@Callback true
                    showAtTime = System.currentTimeMillis()
                }
            }
            WHAT_CANCEL -> {
                showAtTime = null
                timerShowWeakReference.get()?.cancel() ?: return@Callback true
            }
        }
        return@Callback true
    })
    private var delay: Long? = null
        get() = field ?: TIME_DELAY_DEF
    private var minShowTime: Long? = null
    private var showAtTime: Long? = null

    init {
        //防止页面切换消息才发出导致的错误和页面关闭后handler持有导致的空指针或者内存泄漏
        owner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                handler.removeCallbacksAndMessages(null)
                timerShowWeakReference.clear()
                owner.lifecycle.removeObserver(this)
            }
        })

    }

    fun setDelay(delay: Long): TimerHelper {
        this.delay = delay
        return this
    }

    fun setMinShowTime(minShowTime: Long): TimerHelper {
        this.minShowTime = minShowTime
        return this
    }

    fun show(): TimerHelper {
        handler.sendMessageDelayed(
            Message.obtain(handler).apply {
                what = WHAT_SHOW
            }, delay!!
        )
        return this
    }

    fun showNow(): TimerHelper {
        handler.sendEmptyMessage(WHAT_SHOW)
        return this
    }

    fun cancelNow() {
        handler.removeMessages(WHAT_CANCEL)
        handler.sendEmptyMessage(WHAT_CANCEL)
        return
    }

    private fun cancelInMain() {
        if (showAtTime != null) {
            if (minShowTime != null) {
                var delay = System.currentTimeMillis() - showAtTime!!
                delay = if (delay > minShowTime!!) {
                    0L
                } else {
                    minShowTime!! - delay
                }
                handler.sendMessageDelayed(
                    Message.obtain(handler).apply {
                        what = WHAT_CANCEL
                    }, delay
                )
            } else {
                handler.sendMessageDelayed(
                    Message.obtain(handler).apply {
                        what = WHAT_CANCEL
                    }, delay!!
                )
            }
        } else {
            handler.removeMessages(WHAT_SHOW)
        }
    }

    fun cancel() {
        handler.sendEmptyMessage(WHAT_MSG_CANCEL)
    }
}