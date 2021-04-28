package com.munch.pre.lib.base.rv

import android.util.SparseArray
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.util.contains

/**
 * Create by munch1182 on 2021/4/22 10:17.
 */

interface AdapterViewType<V : BaseViewHolder> {

    fun createVH(parent: ViewGroup, viewType: Int): V

    fun createViewByView(parent: ViewGroup, itemView: View): View {
        return itemView
    }

    fun createViewByRes(parent: ViewGroup, @LayoutRes res: Int): View {
        return LayoutInflater.from(parent.context).inflate(res, parent, false)
    }
}

interface MultiTypeWithPos {

    fun getItemTypeByPos(pos: Int): Int
}

interface MultiType<V : BaseViewHolder> : AdapterViewType<V> {

    var viewResMap: SparseIntArray?
    var viewMap: SparseArray<View>?
    var posType: MultiTypeWithPos?

    companion object {

        const val DEF_INITIAL = 2
    }

    fun register(viewType: Int, @LayoutRes viewRes: Int): MultiType<V> {
        if (viewResMap == null) {
            viewResMap = SparseIntArray(DEF_INITIAL)
        }
        viewResMap?.put(viewType, viewRes)
        return this
    }

    fun register(viewType: Int, view: View): MultiType<V> {
        if (viewMap == null) {
            viewMap = SparseArray(DEF_INITIAL)
        }
        viewMap?.put(viewType, view)
        return this
    }

    fun setType(pos: MultiTypeWithPos): MultiType<V> {
        posType = pos
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun createVH(parent: ViewGroup, viewType: Int): V {
        if (viewResMap == null && viewMap == null) {
            throw IllegalStateException("must set item view")
        }
        val itemView =
            when {
                viewResMap != null && viewResMap!!.contains(viewType) ->
                    createViewByRes(parent, viewResMap!![viewType])
                viewMap != null && viewMap!!.indexOfKey(viewType) < 0 ->
                    createViewByView(parent, viewMap!![viewType]!!)
                else -> throw IllegalStateException("must bind view for viewType:$viewType")
            }
        return BaseViewHolder(itemView) as V
    }

}

interface SingleType<V : BaseViewHolder> : AdapterViewType<V> {

    var view: View?
    var res: Int

    @Suppress("UNCHECKED_CAST")
    override fun createVH(parent: ViewGroup, viewType: Int): V {
        val itemView = when {
            res != 0 -> createViewByRes(parent, res)
            view != null -> createViewByView(parent, view!!)
            else -> throw IllegalStateException("must set item view ")
        }
        return BaseViewHolder(itemView) as V
    }
}