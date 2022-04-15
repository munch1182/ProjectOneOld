package com.munch.lib.recyclerview

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.munch.lib.extend.ViewCreator

/**
 * Create by munch1182 on 2022/3/31 14:23.
 */
interface AdapterViewImp<VH : BaseViewHolder> {

    fun createVH(parent: ViewGroup, viewType: Int): VH

    fun getItemViewTypeByPos(position: Int): Int = 0
}

interface ViewCreatorImp {

    fun createView(parent: ViewGroup, @LayoutRes res: Int): View {
        return LayoutInflater.from(parent.context).inflate(res, parent, false)
    }
}

class SingleVHCreator<VH : BaseViewHolder>(
    @LayoutRes private val res: Int = 0,
    private val viewCreator: ViewCreator? = null
) : AdapterViewImp<VH>, ViewCreatorImp {

    @Suppress("UNCHECKED_CAST")
    override fun createVH(parent: ViewGroup, viewType: Int): VH {
        val v = when {
            res != 0 -> createView(parent, res)
            viewCreator != null -> viewCreator.invoke(parent.context)
            else -> throw IllegalArgumentException("cannot create ViewHolder without view")
        }
        return BaseViewHolder(v) as VH
    }
}

class MultiVHCreator<VH : BaseViewHolder>(private val map: SparseArray<SingleVHCreator<VH>>) :
    AdapterViewImp<BaseViewHolder> {

    override fun createVH(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return map.get(viewType)?.createVH(parent, viewType)
            ?: throw IllegalArgumentException("cannot create ViewHolder without view")
    }

    override fun getItemViewTypeByPos(position: Int) = 0

}