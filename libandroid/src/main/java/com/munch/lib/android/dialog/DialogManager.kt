package com.munch.lib.android.dialog

import androidx.activity.ComponentDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.impInMain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

interface DialogManager {
    /**
     * 添加一个dialog到队列中
     */
    fun add(dialog: ComponentDialog): DialogManager

    /**
     * 在一个dialog未显示之前, 将其从队列中移除
     */
    fun remove(dialog: ComponentDialog): DialogManager

    /**
     * 显示队列中的第一个dialog
     */
    fun show()

    /**
     * 取消当前显示的dialog
     */
    fun cancel()

    /**
     * 清除当前队列中的所有dialog
     *
     * 此方法不会取消当前正在显示的dialog
     */
    fun clear()

    /**
     * 在当前dialog消失之后, 使用此方法阻止下一个dialog的显示
     *
     * 使用此方法后, 需要手动调用[show]方法才能显示下一个dialog
     *
     * 此方法只能阻止下一个dialog的显示
     */
    fun pause(): DialogManager

    /**
     * 当前队列中的dialog数量
     */
    val size: Int
}

/**
 * 处理Dialog队列
 */
class DialogManagerImp : DialogManager {

    private val queue: Queue<ComponentDialog> = LinkedList()
    private var curr: ComponentDialog? = null
    private var pause = false

    private val life = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            curr?.lifecycle?.removeObserver(this)
            curr = null
            if (pause) return
            AppHelper.launch {
                delay(100L)
                show()
            }
        }
    }

    override fun add(dialog: ComponentDialog): DialogManagerImp {
        queue.add(dialog)
        return this
    }

    override fun remove(dialog: ComponentDialog): DialogManagerImp {
        queue.remove(dialog)
        return this
    }

    override fun show() {
        pause = false
        if (curr == null && !queue.isEmpty()) {
            curr = queue.poll()
        }
        curr?.let {
            impInMain {
                //添加生命周期回调, 触发下一个dialog
                it.lifecycle.addObserver(life)
                it.show()
            }
        }
    }

    override fun cancel() {
        curr?.cancel()
    }

    override fun clear() {
        queue.clear()
    }

    override fun pause(): DialogManagerImp {
        this.pause = true
        return this
    }

    override val size: Int
        get() = queue.size
}

fun ComponentDialog.offer(m: DialogManager): DialogManager {
    m.add(this)
    return m
}

@Suppress("NOTHING_TO_INLINE")
inline fun androidx.appcompat.app.AlertDialog.Builder.offer(m: DialogManager): DialogManager {
    impInMain { create().offer(m) }
    return m
}