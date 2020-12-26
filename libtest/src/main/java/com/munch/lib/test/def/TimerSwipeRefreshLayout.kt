package com.munch.lib.test.def

import androidx.lifecycle.LifecycleOwner
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.munch.lib.helper.TimerHelper
import com.munch.lib.helper.TimerHelper.Companion.withTimer

/**
 * Create by munch1182 on 2020/12/26 13:43.
 */
class TimerSwipeRefreshLayout(private val srl: SwipeRefreshLayout) : TimerHelper.ITimerShow {
    override fun show() {
        srl.isRefreshing = true
    }

    override fun cancel() {
        srl.isRefreshing = false
    }

    companion object {

        fun SwipeRefreshLayout.withTimer(owner: LifecycleOwner) =
            TimerSwipeRefreshLayout(this).withTimer(owner)

        fun TimerHelper.with(owner: LifecycleOwner, srl: SwipeRefreshLayout): TimerHelper {
            return TimerHelper.with(owner, TimerSwipeRefreshLayout(srl))
        }

    }

}