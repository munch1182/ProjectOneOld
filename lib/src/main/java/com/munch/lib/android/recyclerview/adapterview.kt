package com.munch.lib.android.recyclerview

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.munch.lib.android.helper.ViewCreator

/**
 * Create by munch1182 on 2022/3/31 14:23.
 */
interface AdapterViewImp<VH : BaseViewHolder> {

    fun createVH(parent: ViewGroup, viewType: Int): VH

    fun getItemViewType(position: Int): Int = 0
}

interface ViewCreatorImp {

    fun createView(parent: ViewGroup, @LayoutRes res: Int): View {
        return LayoutInflater.from(parent.context).inflate(res, parent, false)
    }
}

class SingleVHCreator(
    @LayoutRes private val res: Int = 0,
    private val viewCreator: ViewCreator? = null
) : AdapterViewImp<BaseViewHolder>, ViewCreatorImp {

    override fun createVH(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val v = when {
            res != 0 -> createView(parent, res)
            viewCreator != null -> viewCreator.invoke(parent.context)
            else -> throw IllegalArgumentException("cannot create ViewHolder without view")
        }
        return BaseViewHolder(v)
    }
}

class MultiVHCreator(private val map: SparseArray<SingleVHCreator>) :
    AdapterViewImp<BaseViewHolder> {

    override fun createVH(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return map.get(viewType)?.createVH(parent, viewType)
            ?: throw IllegalArgumentException("cannot create ViewHolder without view")
    }

    override fun getItemViewType(position: Int) = 0

}