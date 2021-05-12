package com.munch.project.launcher.base

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.munch.pre.lib.base.BaseRootActivity
import com.munch.pre.lib.base.BaseRootFragment
import com.munch.pre.lib.helper.BarHelper

/**
 * Create by munch1182 on 2021/5/8 10:23.
 */
open class BaseActivity : BaseRootActivity(), TestFun {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        measureHelper.startActivityShow(this)
        toggleTheme()
        super.onCreate(savedInstanceState)
        handleBar()
        delayLoad { measureHelper.stopActivityShow(this) }
    }

    protected open fun handleBar() {
        BarHelper(this).hideStatusBar(true).colorStatusBar(Color.TRANSPARENT)
        window.navigationBarColor = Color.TRANSPARENT
    }

    protected open fun toggleTheme() {
        SwitchHelper.INSTANCE.switchTheme(this)
    }

}

open class BaseFragment : BaseRootFragment(), TestFun {

    private var resId: Int = 0
    private var vb: ViewDataBinding? = null

    @Suppress("UNCHECKED_CAST")
    protected fun <T : ViewDataBinding> bind(@LayoutRes resId: Int): Lazy<T> {
        this.resId = resId
        return lazy {
            vb as T
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        measureHelper.count(this, "onCreateView", 2)
        if (resId == 0) {
            return null
        }
        measureHelper.startFragmentInflate(this)
        vb = DataBindingUtil.inflate(inflater, resId, container, false)
        vb?.lifecycleOwner = this
        measureHelper.stopFragmentInflate(this)
        return vb?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        measureHelper.reset(this, "onCreateView")
        vb?.unbind()
        vb = null
    }
}