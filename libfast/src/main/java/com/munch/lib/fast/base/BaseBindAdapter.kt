package com.munch.lib.fast.base

import android.util.SparseArray
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import com.munch.pre.lib.base.rv.*

/**
 * Create by munch1182 on 2021/3/31 15:27.
 */
abstract class BaseBindAdapter<D, V : ViewDataBinding>(
    override var res: Int,
    data: MutableList<D>? = null
) : BaseAdapter<D, BaseBindViewHolder<V>>(data), SingleType<BaseBindViewHolder<V>> {

    override fun createVH(parent: ViewGroup, viewType: Int): BaseBindViewHolder<V> {
        val from = LayoutInflater.from(parent.context)
        return BaseBindViewHolder(
            DataBindingUtil.inflate(
                from,
                res,
                parent,
                false
            )
        )
    }

    override var view: View? = null
}

abstract class BaseDifferBindAdapter<D, V : ViewDataBinding>(
    override var res: Int,
    callback: DiffUtil.ItemCallback<D>
) : BaseDifferAdapter<D, BaseBindViewHolder<V>>(AsyncDifferConfig.Builder<D>(callback).build()),
    SingleType<BaseBindViewHolder<V>> {

    override fun createVH(parent: ViewGroup, viewType: Int): BaseBindViewHolder<V> {
        val from = LayoutInflater.from(parent.context)
        return BaseBindViewHolder(DataBindingUtil.inflate(from, res, parent, false))
    }

    override var view: View? = null
}

abstract class BaseBindMultiAdapter<D>(
    data: MutableList<D>? = null
) : BaseAdapter<D, BaseBindViewHolder<ViewDataBinding>>(data),
    MultiType<BaseBindViewHolder<ViewDataBinding>> {

    override fun createVH(parent: ViewGroup, viewType: Int): BaseBindViewHolder<ViewDataBinding> {
        if (viewResMap == null) {
            throw IllegalStateException("must set item res layout for view binding")
        }
        val from = LayoutInflater.from(parent.context)
        return BaseBindViewHolder(
            DataBindingUtil.inflate(from, viewResMap!![viewType], parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return posType?.getItemTypeByPos(position) ?: super.getItemViewType(position)
    }

    override var viewResMap: SparseIntArray? = null
    override var viewMap: SparseArray<View>? = null
    override var posType: MultiTypeWithPos? = null

}

abstract class BaseDifferBindMultiAdapter<D>(
    callback: DiffUtil.ItemCallback<D>
) : BaseDifferAdapter<D, BaseBindViewHolder<ViewDataBinding>>(
    AsyncDifferConfig.Builder<D>(callback).build()
), MultiType<BaseBindViewHolder<ViewDataBinding>> {

    override fun createVH(parent: ViewGroup, viewType: Int): BaseBindViewHolder<ViewDataBinding> {
        if (viewResMap == null) {
            throw IllegalStateException("must set item res layout for view binding")
        }
        val from = LayoutInflater.from(parent.context)
        return BaseBindViewHolder(
            DataBindingUtil.inflate(from, viewResMap!![viewType], parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return posType?.getItemTypeByPos(position) ?: super.getItemViewType(position)
    }

    override var viewResMap: SparseIntArray? = null
    override var viewMap: SparseArray<View>? = null
    override var posType: MultiTypeWithPos? = null

}

open class BaseBindViewHolder<V : ViewDataBinding>(open val bind: V) : BaseViewHolder(bind.root) {

    @Suppress("UNCHECKED_CAST")
    fun <VB : ViewDataBinding> getVB(): VB = bind as VB
}