package com.munch.lib.recyclerview

import android.util.SparseArray
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.util.contains

/**
 * Create by munch1182 on 2021/8/5 17:58.
 */

interface AdapterViewImp<VH : BaseViewHolder> {

    fun createVH(parent: ViewGroup, viewType: Int): VH

    fun createViewByView(parent: ViewGroup, itemView: View): View {
        return itemView
    }

    fun createViewByRes(parent: ViewGroup, @LayoutRes res: Int): View {
        return LayoutInflater.from(parent.context).inflate(res, parent, false)
    }
}

interface SingleTypeViewHelper<VH : BaseViewHolder> : AdapterViewImp<VH> {

    var itemView: View?
    var layoutRes: Int

    override fun createVH(parent: ViewGroup, viewType: Int): VH {
        val itemView = when {
            //先检查是否设置了布局文件
            layoutRes != 0 -> createViewByRes(parent, layoutRes)
            //如果没有布局，则再检查是否设置了view
            itemView != null -> createViewByView(parent, itemView!!)
            //如果都没有，则报错
            else -> throw NullPointerException()
        }
        @Suppress("UNCHECKED_CAST")
        return BaseViewHolder(itemView) as VH
    }
}

interface MultiTypeWithPos {

    fun viewTypeWhenPosIs(pos: Int): Int
}

interface MultiTypeViewHelper<VH : BaseViewHolder> : AdapterViewImp<VH> {
    var viewResMap: SparseIntArray?
    var viewMap: SparseArray<View>?
    var posType: MultiTypeWithPos?

    companion object {

        const val DEF_INITIAL = 2
    }

    fun register(type: Int, @LayoutRes layoutRes: Int): MultiTypeViewHelper<VH> {
        if (viewResMap == null) {
            viewResMap = SparseIntArray(DEF_INITIAL)
        }
        viewResMap?.put(type, layoutRes)
        return this
    }

    fun register(type: Int, view: View): MultiTypeViewHelper<VH> {
        if (viewMap == null) {
            viewMap = SparseArray(DEF_INITIAL)
        }
        viewMap?.put(type, view)
        return this
    }

    fun setType(pos: MultiTypeWithPos): MultiTypeViewHelper<VH> {
        posType = pos
        return this
    }

    override fun createVH(parent: ViewGroup, viewType: Int): VH {
        if (viewResMap == null && viewMap == null) {
            throw IllegalStateException("must set item view")
        }
        val itemView =
            when {
                viewResMap?.contains(viewType) == true ->
                    createViewByRes(parent, viewResMap!![viewType])
                (viewMap?.indexOfKey(viewType) ?: 0) < 0 ->
                    createViewByView(parent, viewMap!![viewType]!!)
                else -> throw NullPointerException()
            }
        @Suppress("UNCHECKED_CAST")
        return BaseViewHolder(itemView) as VH
    }
}