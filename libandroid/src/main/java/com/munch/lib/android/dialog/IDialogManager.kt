package com.munch.lib.android.dialog

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.impInMain
import com.munch.lib.android.log.Logger
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

/**
 * 如果[uniqueTag]不为null, 在添加一个返回该参数的Dialog之后, 到该Dialog被取消之前, 添加返回该参数的Dialog的动作会被忽略
 *
 * 该接口需要手动添加到实现的Dialog上
 */
interface DialogUnique {
    val uniqueTag: String?
        get() = null
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

    private val log = Logger.only("dialog").enable(false)
    private var curr: IDialog? = null // 当前正在显示的dialog, 当其取消显示时, 此值为null

    private var addDialogForUnique: MutableList<String>? = null

    private val life2Next = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            val dialog = curr
            if (dialog != null) {
                dialog.lifecycle.removeObserver(this)
                if (dialog is DialogUnique && dialog.uniqueTag != null) {
                    addDialogForUnique?.remove(dialog.uniqueTag)
                }
            }
            curr = null
            AppHelper.launch {
                log.log("dialog onStop, left: ${queue.size}, next.")
                delay(200L)
                show()
            }
        }
    }

    private val life2Null = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            curr?.lifecycle?.removeObserver(this)
            curr = null
            log.log("dialog onStop, left: ${queue.size}, stop as pause.")
        }
    }

    override fun show() {
        // enuqe
        if (curr != null) {
            log.log("show called but dialog is showing.")
            return
        }
        if (!queue.isEmpty()) {
            curr = queue.poll()
        }
        if (curr != null) {
            impInMain {
                log.log("show dialog.")
                //添加生命周期回调, 触发下一个dialog
                curr?.lifecycle?.addObserver(life2Next)
                curr?.show()
            }
        } else {
            log.log("no dialog. complete.")
        }

    }

    override fun dismiss() {
        log.log("dismiss dialog.")
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
            log.log("pause dialog loop.")
            curr?.lifecycle?.removeObserver(life2Next)
            curr?.lifecycle?.addObserver(life2Null)
        }
        return this
    }

    override fun add(dialog: IDialog): IDialogManager {
        if (dialog is DialogUnique && dialog.uniqueTag != null) {
            if (addDialogForUnique == null) {
                addDialogForUnique = mutableListOf()
            }
            val unique = addDialogForUnique
            val tag = dialog.uniqueTag
            if (unique != null && tag != null) {
                if (unique.contains(tag)) {
                    log.log("repeat add DialogUnique, ignore.")
                    return this
                }
                unique.add(tag)
            }
        }
        log.log("add one dialog.")
        return super.add(dialog)
    }

    override fun remove(dialog: IDialog): IDialogManager {
        log.log("remove one dialog.")
        return super.remove(dialog)
    }

    override fun clear() {
        log.log("clear dialog.")
        super.clear()
    }
}