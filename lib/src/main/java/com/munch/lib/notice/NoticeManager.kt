package com.munch.lib.notice

import com.munch.lib.extend.postUI

/**
 * Created by munch1182 on 2022/5/17 21:07.
 */
class ActivityNoticeManager {

    private val stack = ArrayDeque<INotice>()
    private val onCancel = { onCancel() }
    private var cancel = false

    fun add(notice: INotice): ActivityNoticeManager {
        notice.onCancel(onCancel)
        stack.add(notice)
        return this
    }

    fun show() {
        if (stack.firstOrNull()?.isShowing != false) {
            return
        }
        cancel = false
        stack.sortByDescending { it.priority.priority }
        stack.firstOrNull()?.show()
    }

    /**
     * 取消后续Notice的显示
     *
     * 需要在INotice调用cancel之前调用
     *
     * todo 只能去掉优先度更低的notice
     */
    fun cancel(clear: Boolean = false, cancelNow: Boolean = false) {
        val first = stack.firstOrNull() ?: return
        cancel = true
        if (clear) {
            stack.clear()
            stack.add(first)
        }
        if (cancelNow) {
            first.cancel()
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
inline fun INotice.byManager(anm: ActivityNoticeManager) = anm.add(this)