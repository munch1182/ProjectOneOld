package com.munch.lib.android.dialog

import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.AppHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * 处理Dialog队列
 */
class DialogManager : DialogInterface {

    private val queue: Queue<DialogInterface> = LinkedList()
    private var curr: DialogInterface? = null


    /**
     * 将Dialog显示到[LifecycleOwner]中, 如果不调用此方法,
     * 则会在[com.munch.lib.android.helper.ActivityStackHelper]的TopActivity中显示
     */
    fun attach(owner: LifecycleOwner) {
    }

    fun add(dialog: DialogInterface): DialogManager {
        queue.add(dialog)
        return this
    }

    fun remove(dialog: DialogInterface): DialogManager {
        queue.remove(dialog)
        return this
    }

    override val isShowing: Boolean
        get() = curr?.isShowing ?: false

    override fun show() {
        // 如果当前的dialog没有被关闭, 则不能显示下一个dialog
        if (isShowing) return
        cancel() // 将当前dialog置为null
        AppHelper.launch(Dispatchers.Default) {
            delay(500L) // dialog显示之间延时500ms
            if (!queue.isEmpty()) {
                curr = queue.poll() // 取出下一个dialog
                curr?.show()        // 显示下一个dialog
            }
        }
    }

    override fun cancel() {
        curr?.cancel()
        curr = null
    }
}