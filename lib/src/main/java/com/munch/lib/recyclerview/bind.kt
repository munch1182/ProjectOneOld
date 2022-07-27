package com.munch.lib.recyclerview

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.munch.lib.extend.findParameterized
import com.munch.lib.extend.inflate
import com.munch.lib.extend.inflateParent

/**
 * Create by munch1182 on 2022/5/21 15:21.
 */
open class BaseBindViewHolder<B : ViewBinding>(open val bind: B) : BaseViewHolder(bind.root)

abstract class BindRVAdapter<D, VB : ViewBinding>(
    adapterFun: AdapterFunImp<D> = AdapterFunImp.Default(),
    clickHelper: AdapterClickHandler<BaseBindViewHolder<VB>> = AdapterListenerHelper(),
) : BaseRecyclerViewAdapter<D, BaseBindViewHolder<VB>>(
    provider = BindVHCreator<VB>(),
    adapterFun = adapterFun,
    clickHelper = clickHelper
)

class BindVHCreator<VB : ViewBinding> : DefaultVHProvider() {

    init {
        registerViewHolder(0) {
            this::class.java.findParameterized(ViewBinding::class.java)
                ?.inflateParent()
                ?.inflate(LayoutInflater.from(it.context), it, false)
                ?.run { BaseBindViewHolder(this) }
                ?: throw IllegalStateException()
        }
    }
}