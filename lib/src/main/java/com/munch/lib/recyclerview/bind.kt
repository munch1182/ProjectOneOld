package com.munch.lib.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.munch.lib.extend.findParameterized
import com.munch.lib.extend.inflate
import com.munch.lib.extend.lazy
import com.munch.lib.extend.inflateParent

/**
 * Create by munch1182 on 2022/5/21 15:21.
 */
open class BindViewHolder<B : ViewBinding>(open val bind: B) : BaseViewHolder(bind.root)

abstract class BindRVAdapter<D, VB : ViewBinding>(
    adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
    clickHelper: AdapterClickHandler<BindViewHolder<VB>> = AdapterListenerHelper(),
) : BaseRecyclerViewAdapter<D, BindViewHolder<VB>>(
    provider = DefaultVHProvider(),
    adapterFun = adapterFun,
    clickHelper = clickHelper
) {

    private val method by lazy {
        this.javaClass.findParameterized(ViewBinding::class.java)?.inflateParent()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder<VB> {
        val vb = method
            ?.inflate(LayoutInflater.from(parent.context), parent, false)
            ?: throw IllegalStateException("")
        return BindViewHolder(vb as VB)
    }
}