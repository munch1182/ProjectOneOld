package com.munch.pre.lib.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Create by munch1182 on 2021/3/31 10:03.
 */
class BaseRootActivity : AppCompatActivity() {

    fun <V : ViewModel> get(clazz: Class<V>): Lazy<V> =
        lazy { ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(clazz) }
}