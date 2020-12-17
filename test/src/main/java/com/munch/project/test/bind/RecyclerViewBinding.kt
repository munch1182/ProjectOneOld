package com.munch.project.test.bind

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2020/12/7 13:42.
 */

object RecyclerViewBinding {

    @BindingAdapter("adapter")
    fun bindRecyclerViewAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        view.adapter = adapter.apply {
            //recyclerview状态自动恢复
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }
}