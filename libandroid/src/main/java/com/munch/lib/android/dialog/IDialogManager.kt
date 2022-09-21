package com.munch.lib.android.dialog

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.impInMain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * 用于管理dialog队列, 来处理同一时间显示多个dialog的问题
 */
interface IDialogManager {

    /**
     * 添加一个dialog到队列中
     */
    fun add(dialog: IDialog): IDialogManager

    /**
     * 在一个dialog未显示之前, 将其从队列中移除
     */
    fun remove(dialog: IDialog): IDialogManager

    /**
     * 如果有, 则将队列中的第一个dialog, 从队列中移除并显示
     */
    fun show()

    /**
     * 如果有, 取消当前显示的dialog
     */
    fun dismiss()

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
    fun pause(): IDialogManager

    /**
     * 当前队列中的dialog数量
     */
    val size: Int

    /**
     * 移除剩余的dialog并取消当前dialog的显示
     */
    fun destroy() {
        clear()
        dismiss()
    }
}

abstract class DialogManagerByQueue : IDialogManager {
    protected open val queue: Queue<IDialog> = LinkedList()
    override fun add(dialog: IDialog): IDialogManager {
        queue.add(dialog)
        return this
    }

    override fun remove(dialog: IDialog): IDialogManager {
        queue.remove(dialog)
        return this
    }

    override fun clear() {
        queue.clear()
    }

    override val size: Int
        get() = queue.size
}

/**
 * 处理Dialog队列
 */
class DefaultDialogManager : DialogManagerByQueue() {

    private var curr: IDialog? = null // 当前正在显示的dialog, 当其取消显示时, 此值为null

    private val life2Next = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            curr?.lifecycle?.removeObserver(this)
            curr = null
            AppHelper.launch {
                delay(100L)
                show()
            }
        }
    }

    private val life2Null = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            curr?.lifecycle?.removeObserver(this)
            curr = null
        }
    }

    override fun show() {
        if (curr == null && !queue.isEmpty()) {
            curr = queue.poll()
        }
        curr?.let {
            impInMain {
                //添加生命周期回调, 触发下一个dialog
                it.lifecycle.addObserver(life2Next)
                it.show()
            }
        }
    }

    override fun dismiss() {
        curr?.dismiss()
    }

    /**
     * 当当前dialog消失时, 不再轮询显示下一个dialog
     *
     * 如果需要显示下一个, 需要手动调用[show]
     *
     * 如果要清除剩余的dialog, 则需要调用[clear]
     */
    override fun pause(): DefaultDialogManager {
        impInMain {
            curr?.lifecycle?.removeObserver(life2Next)
            curr?.lifecycle?.addObserver(life2Null)
        }
        return this
    }
}