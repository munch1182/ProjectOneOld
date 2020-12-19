package com.munch.project.testsimple.jetpack.bind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.base.BaseLibActivity

/**
 * Create by munch1182 on 2020/12/7 13:42.
 */
inline fun <reified T : ViewDataBinding> BaseLibActivity.binding(@LayoutRes resId: Int): Lazy<T> =
    lazy { DataBindingUtil.setContentView(this, resId) }

inline fun <reified T : ViewDataBinding> RecyclerView.Adapter<out RecyclerView.ViewHolder>.binding(
    parent: ViewGroup,
    @LayoutRes resId: Int
): T {
    return DataBindingUtil.inflate(LayoutInflater.from(parent.context), resId, parent, false)
}
