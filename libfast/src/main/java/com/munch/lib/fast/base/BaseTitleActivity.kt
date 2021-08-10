package com.munch.lib.fast.base

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.appbar.MaterialToolbar
import com.munch.lib.base.ViewHelper
import com.munch.lib.base.dp2Px
import com.munch.lib.base.getColorPrimary
import com.munch.lib.helper.PhoneHelper

/**
 * 为布局统一加上一个[androidx.appcompat.widget.Toolbar]
 * 注意：只是简单的加上一个toolbar，如果复杂的布局，应该自行实现，而不应再在此基类上拓展
 *
 * Create by munch1182 on 2021/8/9 17:48.
 */
open class BaseTitleActivity : BaseActivity() {

    protected open val container: FrameLayout by lazy { findViewById(android.R.id.content) }
    protected open val tb by lazy { MaterialToolbar(this).apply { setToolbar(this) } }

    protected open fun setToolbar(tb: MaterialToolbar) {
        tb.apply {
            setBackgroundColor(getColorPrimary())
            translationZ = dp2Px(3f)

            if (canBack()) {
                navigationIcon = getBackIconWhite()
                setNavigationOnClickListener { onBackPressed() }
            }

            setTitleTextColor(Color.WHITE)
            title = this@BaseTitleActivity::class.java.simpleName.replace("Activity", "")
        }
    }

    protected open fun canBack() = true

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        val actionBarSize = PhoneHelper.getActionBarSize() ?: 0

        val lp = FrameLayout.LayoutParams(
            ViewHelper.newMarginLP(t = actionBarSize, view = view, lpIfNo = params)
        )
        super.setContentView(view, lp)
        container.addView(
            tb, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionBarSize)
        )
    }

    override fun setContentView(view: View?) {
        setContentView(view, ViewHelper.newMWLayoutParams())
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(View.inflate(this, layoutResID, null))
    }

    override fun setTitle(title: CharSequence?) {
        tb.title = title
    }

    override fun setTitle(titleId: Int) {
        tb.setTitle(titleId)
    }
}