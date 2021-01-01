package com.munch.project.test

import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.munch.lib.base.BaseRootActivity

/**
 * Create by munch1182 on 2020/12/7 10:45.
 */
open class BaseActivity : BaseRootActivity() {

    protected inline fun <reified T : ViewDataBinding> binding(@LayoutRes resId: Int): Lazy<T> =
        lazy { DataBindingUtil.setContentView(this, resId) }

}