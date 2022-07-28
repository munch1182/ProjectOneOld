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

    val vhCreator: SparseArray<VHCreator>

    fun registerViewHolder(itemType: Int, creator: VHCreator): VHProvider {
        vhCreator[itemType] = creator
        return this
    }
}

open class DefaultVHProvider : VHProvider {
    override val vhCreator: SparseArray<VHCreator> = SparseArray(2)
}

fun VHProvider.registerViewHolder(itemType: Int, @LayoutRes resId: Int): VHProvider {
    vhCreator[itemType] = { BaseViewHolder(it, resId) }
    return this
}

fun VHProvider.registerViewHolder(itemType: Int, vc: ViewCreator): VHProvider {
    vhCreator[itemType] = { BaseViewHolder(vc.invoke(it.context)) }
    return this
}

class SimpleVHProvider(
    @LayoutRes resId: Int,
    vc: ViewCreator?
) : DefaultVHProvider() {

    constructor(@LayoutRes resId: Int) : this(resId, null)
    constructor(vc: ViewCreator) : this(0, vc)

    init {
        if (vc != null) {
            registerViewHolder(0, vc)
        } else {
            registerViewHolder(0, resId)
        }
    }
}

interface ItemType {
    fun getItemType(pos: Int): Int = 0
}

interface ItemNode : ItemType {
    val children: List<ItemNode>
        get() = listOf()
    val isExpand: Boolean
        get() = false
}