package com.munch.lib.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.munch.lib.base.BaseRootActivity

/**
 * Create by munch1182 on 2021/1/6 17:11.
 */
open class BaseActivity : BaseRootActivity() {

    fun <V : ViewModel> get(clazz: Class<V>): Lazy<V> = lazy { ViewModelProvider(this).get(clazz) }
}