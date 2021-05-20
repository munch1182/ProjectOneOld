package com.munch.test.project.one.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.munch.pre.lib.base.BaseRootFragment
import com.munch.test.project.one.switch.SwitchHelper
import com.munch.lib.fast.base.activity.BaseItemActivity as BIA
import com.munch.lib.fast.base.activity.BaseItemWithNoticeActivity as BIWNA
import com.munch.lib.fast.base.activity.BaseRvActivity as BRA
import com.munch.lib.fast.base.activity.BaseTopActivity as BTA

/**
 * Create by munch1182 on 2021/4/8 15:05.
 */
open class BaseTopActivity : BTA() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchHelper.INSTANCE.switchTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle("$title${SwitchHelper.INSTANCE.getTestSuffix()}")
    }
}

abstract class BaseItemActivity : BIA() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchHelper.INSTANCE.switchTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle("$title${SwitchHelper.INSTANCE.getTestSuffix()}")
    }
}

abstract class BaseItemWithNoticeActivity : BIWNA() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchHelper.INSTANCE.switchTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle("$title${SwitchHelper.INSTANCE.getTestSuffix()}")
    }
}

abstract class BaseRvActivity : BRA() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SwitchHelper.INSTANCE.switchTheme(this)
        super.onCreate(savedInstanceState)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle("$title${SwitchHelper.INSTANCE.getTestSuffix()}")
    }
}

open class BaseFragment : BaseRootFragment() {

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
        if (resId == 0) {
            return null
        }
        vb = DataBindingUtil.inflate(inflater, resId, container, false)
        vb?.lifecycleOwner = this
        return vb?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vb?.unbind()
        vb = null
    }
}