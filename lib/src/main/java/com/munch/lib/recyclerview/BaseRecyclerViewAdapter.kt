package com.munch.lib.recyclerview

import androidx.recyclerview.widget.RecyclerView

/**
 * rv需要考虑的情况:
 * 1. 简单展示的快速使用 --> 由子类实现
 * 2. 有无DiffUtil相关实现下的通用方法(crud)及局部刷新
 * 3. 多类型以及ConcatAdapter --> 由组装的方法实现，并由子类实现常用的，其余的应根据场景自行组装
 * 4. 是否需要对PagingDataAdapter进行兼容 --> AdapterFun
 * 5. dataBinding --> 由子类实现
 * 6. foot,header --> ContactAdapter
 *
 * Create by munch1182 on 2021/8/5 16:27.
 */

/**
 * 实现crud的基本方法，只涉及数据，不涉及页面布局
 *
 * @see AdapterFun
 */
abstract class BaseRecyclerViewAdapter<D, VH : BaseViewHolder> : RecyclerView.Adapter<VH>(),
    AdapterFun<D, VH> {

    protected open val list = mutableListOf<D?>()

    override val data: MutableList<D?>
        get() = list
    override val adapter: BaseRecyclerViewAdapter<D, VH>
        get() = this

    override fun getItemCount() = data.size
}