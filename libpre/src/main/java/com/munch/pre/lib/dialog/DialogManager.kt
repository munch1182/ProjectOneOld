package com.munch.pre.lib.dialog

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.munch.pre.lib.DefaultDepend
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.helper.AppStatusHelper
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ArrayBlockingQueue

/**
 * 用以解决后台弹出dialog的问题
 *
 * 主要让多个不定dialog弹出时依序弹出，而不是一起弹出
 *
 * Create by munch1182 on 2021/4/7 16:43.
 */
@DefaultDepend([BaseApp::class, AppStatusHelper::class])
class DialogManager private constructor() : Observer<Boolean> {

    companion object {

        val INSTANCE by lazy { DialogManager() }

        fun DialogManager.add(creator: (context: Context) -> AlertDialog) {
            add(object : IDialogCreator {
                override fun create(context: Context): IDialog {
                    val dialog = creator.invoke(context)
                    return object : IDialog {
                        override fun show() {
                            dialog.show()
                        }

                        override fun setOnHandleListener(func: () -> Unit): IDialog {
                            dialog.setOnCancelListener {
                                func.invoke()
                            }
                            dialog.setOnDismissListener {
                                func.invoke()
                            }
                            return this
                        }
                    }
                }
            })
        }

        private const val WHAT_ADD = 111
    }

    interface IDialog {

        fun show()

        /**
         * 当dialog操作完成(比如取消显示后)应该调用此方法
         *
         * @return 返回本身
         */
        fun setOnHandleListener(func: () -> Unit): IDialog
    }

    interface IDialogCreator {

        fun create(context: Context): IDialog
    }

    private val queue = ArrayBlockingQueue<IDialogCreator>(10)
    private val mutex = Mutex()
    private val handler = Handler(Looper.getMainLooper()) { msg ->
        if (msg.what == WHAT_ADD) {
            check()
        }
        true
    }

    private fun check() {
        //如果应用不在前台
        if (!AppStatusHelper.isForeground()) {
            //等待前台
            AppStatusHelper.getForegroundLiveData().observeForever(this)
            return
        }
        val topActivity = AppStatusHelper.getTopActivity()
        //如果页面正在切换
        if (topActivity == null) {
            //等待页面
            BaseApp.getInstance().getMainHandler().postDelayed({ check() }, 500L)
            return
        }
        runBlocking {
            val creator: IDialogCreator
            mutex.withLock {
                creator = (queue.poll() ?: return@runBlocking)
            }
            //显示dialog
            creator.create(topActivity)
                .setOnHandleListener { check() }
                .show()
        }
    }

    fun add(creator: IDialogCreator) {
        runBlocking {
            mutex.withLock {
                if (!queue.contains(creator)) {
                    queue.put(creator)
                    handler.removeMessages(WHAT_ADD)
                    handler.sendMessageDelayed(Message.obtain(handler, WHAT_ADD), 500L)
                }
            }
        }
    }

    fun addGroup(vararg creator: IDialogCreator) {
        creator.forEach { add(it) }
    }

    fun remove(creator: IDialogCreator) {
        runBlocking {
            mutex.withLock {
                if (queue.contains(creator)) {
                    queue.remove(creator)
                }
            }
        }
    }

    override fun onChanged(foreground: Boolean) {
        if (foreground) {
            AppStatusHelper.getForegroundLiveData().removeObserver(this)
            check()
        }
    }
}