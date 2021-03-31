package com.munch.lib.fast.extend

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Create by munch1182 on 2021/3/31 9:54.
 */
fun <V : ViewDataBinding> Activity.bind(@LayoutRes resId: Int): Lazy<V> {
    return lazy { DataBindingUtil.setContentView(this, resId) }
}

fun <M : ViewModel> ComponentActivity.get(target: Class<M>): Lazy<M> {
    return lazy { ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(target) }
}