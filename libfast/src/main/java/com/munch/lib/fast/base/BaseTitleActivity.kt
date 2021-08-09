package com.munch.lib.fast.base

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.munch.lib.app.AppHelper
import com.munch.lib.base.setMarginOrKeep
import com.munch.lib.helper.PhoneHelper

/**
 * 为布局统一加上一个[androidx.appcompat.widget.Toolbar]
 * 注意：只是简单的加上一个toolbar，如果复杂的布局，应该自行实现，而不是再在此类上拓展
 *
 * Create by munch1182 on 2021/8/9 17:48.
 */
open class BaseTitleActivity : BaseActivity() {

    protected val container: FrameLayout by lazy { findViewById(android.R.id.content) }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        view?.setMarginOrKeep(t = PhoneHelper.getActionBarSize() ?: 0)
        super.setContentView(view, params)

    }

    override fun setContentView(view: View?) {
        setContentView(view, null)
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(View.inflate(this, layoutResID, container))
    }
}