package com.munch.project.launcher.base

import android.content.Context
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
import com.munch.pre.lib.helper.AppHelper

/**
 * Create by munch1182 on 2021/3/31 15:27.
 */
abstract class BaseBindAdapter<D, V : ViewDataBinding>(
    override var res: Int,
    data: MutableList<D>? = null
) : BaseAdapter<D, BaseBindViewHolder<V>>(data), SingleType<BaseBindViewHolder<V>> {

    override fun createVH(parent: ViewGroup, viewType: Int): BaseBindViewHolder<V> {
        val from = LayoutInflater.from(parent.context)
        return BaseBindViewHolder(DataBindingUtil.inflate(from, res, parent, false))
    }

    override var view: View? = null
}

abstract class BaseDifferBindAdapter<D, V : ViewDataBinding>(
    override var res: Int,
    callback: DiffUtil.ItemCallback<D>
) : BaseDifferAdapter<D, BaseBindViewHolder<V>>(AsyncDifferConfig.Builder(callback).build()),
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
    AsyncDifferConfig.Builder(callback).build()
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

/**
 * ConcatAdapter.Config.Builder()
 *      //SHARED_STABLE_IDS需要每个adapter的STABLE_ID保持唯一
 *      //如果ConcatAdapter使用STABLE_ID，则所有加入的adapter都需要设置setHasStableIds(true)
 *   .setStableIdMode(ConcatAdapter.Config.StableIdMode.SHARED_STABLE_IDS)
 *   .build(),
 */
class StatusAdapter(context: Context) : SimpleAdapter<Int>(View(context).apply {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        AppHelper.PARAMETER.getStatusBarHeight()
    )
}, arrayListOf(1)) {

    init {
        setHasStableIds(true)
    }

    companion object {
        const val STABLE_ID = 10000L
    }

    override fun getItemId(position: Int): Long {
        return STABLE_ID
    }
}
