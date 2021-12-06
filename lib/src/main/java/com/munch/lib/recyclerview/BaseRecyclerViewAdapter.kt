package com.munch.lib.recyclerview

import android.os.Handler
import android.os.Looper
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
 * 7. 要尽量避免向外部调用暴露无关的实现方法
 *
 * Create by munch1182 on 2021/8/5 16:27.
 */
/**
 * 实现crud的基本方法，
 * 以及页面布局的基本实现（仍需要在实现类中标记页面布局[SingleViewModule]/[MultiViewModule]或自定义实现）
 *
 * @see AdapterFunImp
 * @see AdapterViewImp
 * @see SingleViewModule
 * @see MultiViewModule
 */
abstract class BaseRecyclerViewAdapter<D, VH : BaseViewHolder> :
    RecyclerView.Adapter<VH>(), IAdapterFun<D>, IsAdapter, AdapterListener {

    protected open val list = mutableListOf<D?>()

    /**
     * @see AdapterListUpdateInHandlerCallback
     */
    protected open val differ: AsyncListDiffer<D>?
        get() = null
    val data: MutableList<D?>
        //注意：differ?.currentList返回的list为不可修改的list
        //不要直接调用此属性来更改数据，使用IAdapterFun中的方法来修改
        get() = differ?.currentList ?: list

    protected val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    override val noTypeAdapter: BaseRecyclerViewAdapter<*, *>
        get() = this

    protected open var adapterViewHelper: AdapterViewImp? = null
    protected open val adapterFunImp: AdapterFunImp<D> by lazy {
        val d = differ
        if (d == null) {
            AdapterFunImp.Default(this, list, mainHandler)
        } else {
            AdapterFunImp.Differ(this, d, runnable)
        }
    }

    /**
     * 数据差异计算完成的回调
     */
    protected open val runnable: Runnable? = null

    init {
        checkAdapterView()
    }

    /**
     * 检查并设置视图实现
     * 如果自实现了[AdapterViewImp]，需要在实现类中覆盖此对象
     */
    protected fun checkAdapterView() {
        adapterViewHelper = when (this) {
            is SingleViewModule -> SingleViewHelper()
            is MultiViewModule -> MultiViewHelper()
            else -> return
        }
    }

    override fun getItemCount() = data.size

    override fun getItemViewType(position: Int): Int {
        val helper = adapterViewHelper
            ?: throw IllegalStateException("must implement AdapterViewImp, such as SingleViewModule and MultiViewModule")
        return helper.getItemViewType(position)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val helper = adapterViewHelper
            ?: throw IllegalStateException("must implement AdapterViewImp, such as SingleViewModule and MultiViewModule")
        return helper.createVH(parent, viewType) as VH
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

    override fun setOnItemClickListener(listener: OnItemClickListener?) =
        clickHelper.setOnItemClickListener(listener)

    override fun setOnItemLongClickListener(listener: OnItemClickListener?) =
        clickHelper.setOnItemLongClickListener(listener)

    override fun setOnViewClickListener(listener: OnItemClickListener?, vararg ids: Int) =
        clickHelper.setOnViewClickListener(listener, *ids)

    override fun setOnViewLongClickListener(listener: OnItemClickListener?, vararg ids: Int) =
        clickHelper.setOnViewLongClickListener(listener, *ids)
    //</editor-fold>

    //<editor-fold desc="AdapterFun">
    override fun set(newData: List<D?>?) = adapterFunImp.set(newData)
    override fun add(element: D?) = adapterFunImp.add(element)
    override fun add(index: Int, element: D?) = adapterFunImp.add(index, element)
    override fun add(elements: Collection<D?>) = adapterFunImp.add(elements)
    override fun add(index: Int, elements: Collection<D?>) = adapterFunImp.add(index, elements)
    override fun remove(element: D) = adapterFunImp.remove(element)
    override fun remove(index: Int) = adapterFunImp.remove(index)
    override fun remove(startIndex: Int, size: Int) = adapterFunImp.remove(startIndex, size)
    override fun remove(element: Collection<D?>) = adapterFunImp.remove(element)
    override fun get(index: Int) = adapterFunImp.get(index)
    override fun getIndex(element: D) = adapterFunImp.getIndex(element)
    override fun contains(element: D) = adapterFunImp.contains(element)
    override fun update(index: Int, element: D?, payload: Any?) =
        adapterFunImp.update(index, element, payload)

    override fun updateOrThrow(index: Int, element: D?, payload: Any?) =
        adapterFunImp.updateOrThrow(index, element, payload)

    override fun update(startIndex: Int, elements: Collection<D?>, payload: Any?) =
        adapterFunImp.update(startIndex, elements, payload)

    override fun updateOrThrow(startIndex: Int, elements: Collection<D?>, payload: Any?) =
        adapterFunImp.updateOrThrow(startIndex, elements, payload)
    //</editor-fold>
}