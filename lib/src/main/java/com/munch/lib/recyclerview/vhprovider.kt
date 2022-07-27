package com.munch.lib.recyclerview

import android.util.SparseArray
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.munch.lib.extend.ViewCreator

/**
 * Create by munch1182 on 2022/3/31 14:23.
 */
fun interface VHCreator {

    fun onCreateVH(parent: ViewGroup): BaseViewHolder
}

interface VHProvider {

    val map: SparseArray<VHCreator>

    fun registerViewHolder(itemType: Int, creator: VHCreator): VHProvider {
        map[itemType] = creator
        return this
    }
}

open class DefaultVHProvider : VHProvider {
    override val map: SparseArray<VHCreator> = SparseArray()
}

class SimpleVHProvider(
    @LayoutRes private val resId: Int,
    private val vc: ViewCreator?
) : DefaultVHProvider() {

    constructor(@LayoutRes resId: Int) : this(resId, null)
    constructor(vc: ViewCreator) : this(0, vc)

    init {
        if (vc != null) {
            registerViewHolder(0) { BaseViewHolder(vc.invoke(it.context)) }
        } else {
            registerViewHolder(0) { BaseViewHolder(it, resId) }
        }

    }
}

interface TypeItem {

    fun getItemType(): Int
}