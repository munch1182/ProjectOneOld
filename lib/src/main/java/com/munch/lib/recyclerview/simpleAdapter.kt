package com.munch.lib.recyclerview

import android.content.Context
import android.util.SparseArray
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.util.forEach
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.munch.lib.base.ViewCreator

/**
 * 用于快速实现RecyclerView的类，主要是简化BaseRecyclerViewAdapter的代码
 *
 * Create by munch1182 on 2021/8/8 0:30.
 */

/**
 * 用于固定显示数据，而不是从其他数据源获取数据时，即少使用[set]来更新数据时使用
 */
open class SimpleAdapter<D> private constructor(
    private val layoutRes: Int = 0,
    private val viewCreator: ((Context) -> View)? = null,
    private val initData: MutableList<D>? = null
) : BaseRecyclerViewAdapter<D, BaseViewHolder>(), SingleViewModule {

    constructor(@LayoutRes layoutRes: Int, initData: MutableList<D>? = null)
            : this(layoutRes, null, initData)

    constructor(viewCreator: ((Context) -> View), initData: MutableList<D>? = null)
            : this(0, viewCreator, initData)

    init {
        setContentView()
    }

    private fun setContentView() {
        singleViewHelper.setContentView(layoutRes)
        if (viewCreator != null) {
            singleViewHelper.setContentView(viewCreator)
        }
        if (initData != null && initData.isNotEmpty()) {
            data.addAll(initData)
        }
    }
}

/**
 * 从其他数据源获取数据因此常使用[set]来更新数据时使用
 */
open class SimpleDiffAdapter<D> private constructor(
    private val layoutRes: Int = 0,
    private val viewCreator: ((Context) -> View)? = null,
    diffUtil: DiffUtil.ItemCallback<D>
) : BaseRecyclerViewAdapter<D, BaseViewHolder>(), SingleViewModule {

    constructor(@LayoutRes layoutRes: Int, diffUtil: DiffUtil.ItemCallback<D>)
            : this(layoutRes, null, diffUtil)

    constructor(viewCreator: ((Context) -> View), diffUtil: DiffUtil.ItemCallback<D>)
            : this(0, viewCreator, diffUtil)

    private val asyncDiffer by lazy {
        AsyncListDiffer(
            AdapterListUpdateCallback(this), AsyncDifferConfig.Builder(diffUtil).build()
        )
    }

    override val differ: AsyncListDiffer<D>
        get() = asyncDiffer

    init {
        setContentView()
    }

    private fun setContentView() {
        singleViewHelper.setContentView(layoutRes)
        if (viewCreator != null) {
            singleViewHelper.setContentView(viewCreator)
        }
    }
}

open class SimpleMutliAdapter<D>(
    getter: ItemViewTypeGetter? = null,
    map: SparseArray<Any>? = null,
    private val initData: MutableList<D>? = null,
) : BaseRecyclerViewAdapter<D, BaseViewHolder>(), MultiViewModule {

    constructor() : this(null, null)

    init {
        setMultiType(getter, map)
    }

    private fun setMultiType(getter: ItemViewTypeGetter?, arrayMap: SparseArray<Any>?) {
        getter ?: return
        arrayMap ?: return
        multiViewHelper.setType(getter)
        arrayMap.forEach { key, value ->
            when (value) {
                is Int -> multiViewHelper.setTypeView(key, value)
                is ViewCreator -> multiViewHelper.setTypeView(key, value)
                else -> throw IllegalStateException()
            }
        }
        if (initData != null) {
            data.addAll(initData)
        }
    }

}