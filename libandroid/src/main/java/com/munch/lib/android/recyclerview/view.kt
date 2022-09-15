package com.munch.lib.android.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.munch.lib.android.define.ViewCreator
import java.lang.reflect.Method

class SingleLayoutVHProvider<VH : BaseViewHolder>(@LayoutRes private val layoutId: Int) :
    VHProvider<VH> {

    @Suppress("UNCHECKED_CAST")
    override fun provideVH(parent: ViewGroup, viewType: Int): VH {
        return BaseViewHolder(
            LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        ) as VH
    }
}

class SingleViewVHProvider<VH : BaseViewHolder>(private val vc: ViewCreator) : VHProvider<VH> {

    @Suppress("UNCHECKED_CAST")
    override fun provideVH(parent: ViewGroup, viewType: Int): VH {
        return BaseViewHolder(vc.invoke(parent.context)) as VH
    }
}

class SingleVBVHProvider<VB : ViewBinding, VH : BaseBindViewHolder<VB>>(var method: Method?) :
    VHProvider<VH> {

    @Suppress("UNCHECKED_CAST")
    override fun provideVH(parent: ViewGroup, viewType: Int): VH {
        val vb = method?.invoke(null, LayoutInflater.from(parent.context), parent, false) as? VB
            ?: throw IllegalStateException("error inflate ViewBinding")
        return BaseBindViewHolder(vb) as VH
    }
}
