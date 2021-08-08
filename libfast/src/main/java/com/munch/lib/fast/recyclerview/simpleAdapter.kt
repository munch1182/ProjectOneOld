package com.munch.lib.fast.recyclerview

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter
import com.munch.lib.recyclerview.BaseViewHolder
import com.munch.lib.recyclerview.SingleViewModule
import com.munch.lib.recyclerview.setContentView

/**
 * 用于固定显示数据，而不是从其他数据源获取数据时，即少使用[set]来更新数据时使用
 */
open class SimpleAdapter<D, VB : ViewDataBinding> constructor(
    @LayoutRes layoutRes: Int = 0,
    private val initData: MutableList<D>? = null,
    private val bindVH: ((BaseDBViewHolder, VB, D?) -> Unit)? = null
) : BaseDBAdapter<D, VB, BaseDBViewHolder>(layoutRes) {

    constructor(@LayoutRes layoutRes: Int, bindVH: ((BaseDBViewHolder, VB, D?) -> Unit)?)
            : this(layoutRes, null, bindVH)

    init {
        setInItData()
    }

    private fun setInItData() {
        if (initData != null && initData.isNotEmpty()) {
            data.addAll(initData)
        }
    }

    override fun onBindViewHolder(holder: BaseDBViewHolder, db: VB, bean: D?) {
        bindVH?.invoke(holder, db, bean)
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