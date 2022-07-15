package com.munch.lib.notice

import com.munch.lib.Priority
import com.munch.lib.extend.postUI

/**
 * Created by munch1182 on 2022/5/17 21:07.
 */
class ActivityNoticeManager {

    private val stack = ArrayDeque<Notice>()
    private val onCancel = { onCancel() }
    private var cancel = false

    fun add(notice: Notice): ActivityNoticeManager {
        notice.addOnCancel(onCancel)
        stack.add(notice)
        return this
    }

    fun show() {
        synchronized(this){
            if (stack.firstOrNull()?.isShowing != false) {
                return
            }
            cancel = false
            stack.sortByDescending { it.priority.priority }
            stack.firstOrNull()?.show()
        }
    }

    /**
     * 隐藏当前显示的notice，并将其移除
     */
    fun cancel() {
        stack.firstOrNull()?.cancel()
    }

    /**
     * 移除未显示的、Priority比[priority]低的notice
     */
    fun removeOthers(priority: Priority = Priority(Int.MAX_VALUE)) {
        val first = stack.firstOrNull() ?: return
        val list = stack.filter { it.priority > priority }
        list.forEach { stack.remove(it) }
        if (first.isShowing) {
            stack.addFirst(first)
        }
    }

    private fun onCancel() {
        val first = stack.removeFirstOrNull()
        first?.cancel()

        if (cancel) {
            return
        }
        postUI(300L) {
            if (!cancel) {
                show()
            }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Notice.byManager(anm: ActivityNoticeManager) = anm.add(this)