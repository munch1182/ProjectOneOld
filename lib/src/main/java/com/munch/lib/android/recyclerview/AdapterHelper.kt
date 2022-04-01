package com.munch.lib.android.recyclerview

import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2022/4/1 10:12.
 */
class AdapterHelper<D, VH : BaseViewHolder>(adapter: BaseRecyclerViewAdapter<D, VH>) :
    IAdapterFun<D> by adapter,
    AdapterClickListener<VH> by adapter {

    private var refreshAdapter = RefreshAdapter()
    private var loadMoreAdapter = LoadMoreAdapter()
    private var emptyAdapter = EmptyAdapter()
    private var concatAdapter =
        ConcatAdapter(refreshAdapter, emptyAdapter, adapter, loadMoreAdapter)

    fun bind(rv: RecyclerView): AdapterHelper<D, VH> {
        rv.adapter = concatAdapter
        return this
    }

    fun showEmpty() {
        emptyAdapter.show()
    }

    fun hideEmpty() {
        emptyAdapter.hide()
    }

    fun showRefresh() {
        refreshAdapter.show()
    }

    fun hideRefresh() {
        refreshAdapter.hide()
    }

    fun showLoadMore() {
        loadMoreAdapter.show()
    }

    fun hideLoadMore() {
        loadMoreAdapter.hide()
    }
}