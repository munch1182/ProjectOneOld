package com.munch.lib.fast.base

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.munch.lib.fast.R
import com.munch.pre.lib.extend.ViewHelper
import com.munch.pre.lib.extend.getColorCompat
import com.munch.pre.lib.extend.setMargin
import com.munch.pre.lib.helper.AppHelper

/**
 * Create by munch1182 on 2021/3/31 9:50.
 */
open class BaseTopActivity : BaseActivity() {

    fun <V : ViewDataBinding> bind(@LayoutRes resId: Int): Lazy<V> {
        return lazy {
            setContentView(resId)
            //因为是先添加的contentView，再添加的top
            val contentView = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
            return@lazy DataBindingUtil.bind<V>(contentView)!!
        }
    }

    protected open val appBar by lazy {
        View.inflate(this, R.layout.activity_base_top, null) as AppBarLayout
    }
    protected open val toolbar: MaterialToolbar? by lazy { appBar.findViewById(R.id.top_tool_bar) }

    override fun setTitle(@StringRes titleId: Int) {
        title = getString(titleId)
    }

    override fun setTitle(title: CharSequence?) {
        toolbar?.title = title
        toolbar?.setTitleTextColor(getColorCompat(R.color.colorWhite))
    }

    override fun setContentView(view: View) {
        setContentView(view, null)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        val layoutParams = when {
            params != null -> ViewGroup.MarginLayoutParams(params)
            view.layoutParams != null -> ViewGroup.MarginLayoutParams(view.layoutParams)
            else -> ViewHelper.newMarginParamsMW()
        }
        view.setMargin(0, AppHelper.PARAMETER.getActionBarSize(), 0, 0, true, layoutParams)
        super.setContentView(view, layoutParams)

        findViewById<ViewGroup>(android.R.id.content).addView(appBar, ViewHelper.newParamsMW())

        setToolBar(toolbar)
    }

    protected open fun setToolBar(toolbar: Toolbar?) {
        title = this::class.java.simpleName.replace("Activity", "")
        val backIcon = getBackIcon()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            backIcon?.setTint(Color.WHITE)
        }
        toolbar?.navigationIcon = backIcon
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    fun noBack() {
        toolbar?.navigationIcon = null
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        setContentView(View.inflate(this, layoutResID, null))
    }

    private fun getBackIcon(): Drawable? {
        val a = obtainStyledAttributes(intArrayOf(android.R.attr.homeAsUpIndicator))
        val result = a.getDrawable(0)
        a.recycle()
        return result
    }
}