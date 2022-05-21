package com.munch.lib.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.munch.lib.extend.inflate
import kotlin.reflect.KClass

/**
 * Create by munch1182 on 2022/5/21 15:21.
 */
open class BaseBindViewHolder<B : ViewBinding>(open val bind: B) : BaseViewHolder(bind.root)

abstract class BindRVAdapter<D, VB : ViewBinding>(
    vb: KClass<VB>,
    adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
    clickHelper: AdapterClickHandler<BaseBindViewHolder<VB>> = AdapterListenerHelper(),
) : BaseRecyclerViewAdapter<D, BaseBindViewHolder<VB>>(
    viewImp = BindVHCreator(vb),
    adapterFun = adapterFun,
    clickHelper = clickHelper
)

class BindVHCreator<VB : ViewBinding, VH : BaseBindViewHolder<VB>>(private val vb: KClass<VB>) :
    AdapterViewImp<VH>, ViewCreatorImp {

    @Suppress("UNCHECKED_CAST")
    override fun createVH(parent: ViewGroup, viewType: Int): VH {
        val bind = inflateMethod().inflate(LayoutInflater.from(parent.context), parent, false) as VB
        return BaseBindViewHolder(bind) as VH
    }

    private fun inflateMethod() = vb.java.getDeclaredMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )
}