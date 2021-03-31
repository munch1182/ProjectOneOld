package com.munch.lib.fast.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.munch.lib.fast.extend.bind
import com.munch.lib.fast.extend.get

/**
 * Create by munch1182 on 2021/3/31 9:50.
 */
open class BaseFastActivity<V : ViewDataBinding, M : ViewModel>(resId: Int, target: Class<M>) :
    BaseActivity() {

    protected val bind by bind<V>(resId)
    protected val model by get(target)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
    }

}