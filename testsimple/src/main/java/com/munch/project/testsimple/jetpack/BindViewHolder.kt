package com.munch.project.testsimple.jetpack

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.munch.project.testsimple.jetpack.bind.inflateByBing

/**
 * Create by munch1182 on 2020/12/20 18:07.
 */
class BindViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    constructor(@LayoutRes resId: Int, parent: ViewGroup) : this(parent.inflateByBing(resId))

    @Suppress("UNCHECKED_CAST")
    fun <T : ViewDataBinding> asBind() = binding as T

    inline fun <T : ViewDataBinding> executeBinding(func: (binding: T) -> Unit): T {
        return asBind<T>().apply {
            func(this)
            executePendingBindings()
        }
    }
}