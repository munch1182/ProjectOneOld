package com.munch.lib.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.munch.lib.extend.findParameterized
import com.munch.lib.extend.inflate
import com.munch.lib.extend.inflateParent
import com.munch.lib.extend.lazy

/**
 * Create by munch1182 on 2022/5/21 15:21.
 */
open class BindViewHolder<B : ViewBinding>(open val bind: B) : BaseViewHolder(bind.root)

abstract class BindRVAdapter<D, VB : ViewBinding>(
    adapterFun: IAdapterFun<D> = AdapterFunImp2(),
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

abstract class BaseBindRvAdapter<D>(
    adapterFun: IAdapterFun<D> = AdapterFunImp2(),
    clickHelper: AdapterClickHandler<BindViewHolder<ViewBinding>> = AdapterListenerHelper(),
) : BaseRecyclerViewAdapter<D, BindViewHolder<ViewBinding>>(0, adapterFun, clickHelper)

inline fun <reified VB : ViewBinding> BaseBindRvAdapter<*>.registerViewHolder(itemType: Int): BaseBindRvAdapter<*> {
    vhCreator[itemType] = {
        BindViewHolder(
            VB::class.java.inflateParent()?.inflate(LayoutInflater.from(it.context), it, false)
                ?: throw IllegalStateException()
        )
    }
    return this
}
