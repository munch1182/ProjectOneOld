package com.munch.project.testsimple.jetpack.bind

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Create by munch1182 on 2020/12/7 13:42.
 */

class GlobeViewBinding {

    @BindingAdapter("adapter")
    fun bindRecyclerViewAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        view.adapter = adapter.apply {
            //recyclerview状态自动恢复
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    @BindingAdapter("refresh")
    fun bindSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout, refresh: Boolean) {
        swipeRefreshLayout.isRefreshing = refresh
    }
}