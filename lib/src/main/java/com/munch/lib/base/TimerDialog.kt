package com.munch.lib.base

import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * Create by munch1182 on 2020/12/25 17:24.
 */
class TimerDialog private constructor(private val dialog: Dialog) {

    companion object {
        const val WHAT_SHOW = 0
        const val WHAT_CANCEL = 1

        private const val TIME_DELAY_DEF = 500L

        fun with(dialog: Dialog): TimerDialog {
            return TimerDialog(dialog)
        }

        fun Dialog.asTimeDialog() = with(this)
    }

    private val handler = Handler(Looper.getMainLooper(), Handler.Callback {
        when (it.what) {
            WHAT_SHOW -> {
                dialog.show()
                showAtTime = System.currentTimeMillis()
            }
            WHAT_CANCEL -> {
                dialog.cancel()
                showAtTime = null
            }
        }
        return@Callback true
    })
    private var delay: Long? = null
        get() = field ?: TIME_DELAY_DEF
    private var maxShowTime: Long? = null
    private var showAtTime: Long? = null

    fun setDelay(delay: Long): TimerDialog {
        this.delay = delay
        return this
    }

    fun setMaxShowTime(maxShowTime: Long): TimerDialog {
        this.maxShowTime = maxShowTime
        return this
    }

    fun show() {
        handler.sendMessageDelayed(
            Message.obtain(handler).apply {
                what = WHAT_SHOW
            }, delay!!
        )
    }

    fun cancel() {
        if (dialog.isShowing) {
            if (maxShowTime != null) {
                var delay: Long
                if (showAtTime == null) {
                    delay = 0L
                } else {
                    delay = System.currentTimeMillis() - showAtTime!!
                    delay = if (delay > maxShowTime!!) {
                        0L
                    } else {
                        maxShowTime!! - delay
                    }
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
}