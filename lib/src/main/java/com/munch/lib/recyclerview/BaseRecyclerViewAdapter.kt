package com.munch.lib.recyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView

/**
 * rv需要考虑的情况:
 * 1. 简单展示的快速使用 --> 由子类实现
 * 2. 有无DiffUtil相关实现下的通用方法(crud)及局部刷新 --> AdapterFun
 * 3. 多类型以及ConcatAdapter --> 由组装的方法实现，并由子类实现常用的，其余的应根据场景自行组装
 * 4. 是否需要对PagingDataAdapter进行兼容 --> AdapterFun
 * 5. dataBinding --> 由子类实现
 * 6. foot,header --> ContactAdapter
 *
 * Create by munch1182 on 2021/8/5 16:27.
 */

/**
 * 实现crud的基本方法，以及页面布局
 *
 * @see AdapterFun
 * @see AdapterViewImp
 * @see SingleViewModule
 * @see MultiViewModule
 */
abstract class BaseRecyclerViewAdapter<D, VH : BaseViewHolder> :
    RecyclerView.Adapter<VH>(), AdapterFun<D>, IsAdapter, AdapterListener {

    protected open val list = mutableListOf<D?>()

    override val differ: AsyncListDiffer<D>?
        get() = null
    override val data: MutableList<D?>
        //注意：differ?.currentList返回的list为不可修改的list
        get() = differ?.currentList ?: list

    override val noTypeAdapter: BaseRecyclerViewAdapter<*, *>
        get() = this
    val adapter: BaseRecyclerViewAdapter<D, VH>
        get() = this

    internal var adapterViewHelper: AdapterViewImp? = null

    init {
        checkAdapterView()
    }

    /**
     * 检查并设置视图实现
     */
    private fun checkAdapterView() {
        adapterViewHelper = when (this) {
            is SingleViewModule -> SingleViewHelper()
            is MultiViewModule -> MultiViewHelper()
            else -> return
        }
    }

    override fun getItemCount() = data.size

    override fun getItemViewType(position: Int): Int {
        check(adapterViewHelper != null) { throw IllegalStateException("必须实现AdapterViewImp,可选SingleViewModule或MultiViewModule") }
        return adapterViewHelper!!.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        check(adapterViewHelper != null) { throw IllegalStateException("必须实现AdapterViewImp,可选SingleViewModule或MultiViewModule") }
        @Suppress("UNCHECKED_CAST")
        return adapterViewHelper!!.createVH(parent, viewType) as VH
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        handleClick(holder)
    }

    //<editor-fold desc="click">
    protected open val clickHelper = AdapterListenerHelper()

    protected open fun handleClick(holder: VH) {
        holder.setOnItemClickListener(clickHelper.itemClickListener)
        holder.setOnItemLongClickListener(clickHelper.itemLongClickListener)
        if (clickHelper.clickIds.isNotEmpty()) {
            holder.setOnViewClickListener(clickHelper.viewClickListener, *clickHelper.clickIds)
        }
        if (clickHelper.longClickIds.isNotEmpty()) {
            holder.setOnViewClickListener(
                clickHelper.viewLongClickListener, *clickHelper.longClickIds
            )
        }
    }

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        clickHelper.setOnItemClickListener(listener)
    }

    override fun setOnItemLongClickListener(listener: OnItemClickListener?) {
        clickHelper.setOnItemLongClickListener(listener)
    }

    override fun setOnViewClickListener(listener: OnItemClickListener?, vararg ids: Int) {
        clickHelper.setOnViewClickListener(listener, *ids)
    }

    override fun setOnViewLongClickListener(listener: OnItemClickListener?, vararg ids: Int) {
        clickHelper.setOnViewLongClickListener(listener, *ids)
    }
    //</editor-fold>
}