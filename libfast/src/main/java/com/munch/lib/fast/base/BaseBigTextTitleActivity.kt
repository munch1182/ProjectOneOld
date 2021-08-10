package com.munch.lib.fast.base

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.munch.lib.base.ViewHelper
import com.munch.lib.fast.R
import com.munch.lib.helper.BarHelper
import com.munch.lib.helper.PhoneHelper

/**
 * 用大文字当作标题的基类
 *
 * Create by munch1182 on 2021/8/10 16:20.
 */
open class BaseBigTextTitleActivity : BaseActivity() {

    protected open val container: FrameLayout by lazy { findViewById(android.R.id.content) }
    protected open val titleTv: TextView by lazy { findViewById(R.id.title_tv) }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        val titleView = View.inflate(this, R.layout.layout_big_text_title, null)
        titleView.measure(0, 0)
        val statusBarHeight = PhoneHelper.getStatusBarHeight() ?: 0
        val lp = FrameLayout.LayoutParams(
            ViewHelper.newMarginLP(
                t = titleView.measuredHeight + statusBarHeight,
                view = view,
                lpIfNo = params
            )
        )
        super.setContentView(view, lp)

        container.addView(
            titleView,
            FrameLayout.LayoutParams(ViewHelper.newMarginLP(statusBarHeight, view = titleView))
        )
        titleView.findViewById<View>(R.id.title_back).apply {
            if (canBack()) {
                setOnClickListener { onBackPressed() }
            } else {
                visibility = View.INVISIBLE
            }
        }

        title = this::class.java.simpleName.replace("Activity", "")

        setBar()
    }

    protected open fun setBar() {
        BarHelper(this).apply {
            colorStatusBar(Color.WHITE)
            setTextColorBlack(true)
        }
    }

    override fun setContentView(view: View?) {
        setContentView(view, ViewHelper.newMWLayoutParams())
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(View.inflate(this, layoutResID, null))
    }

    override fun setTitle(titleId: Int) {
        titleTv.setText(titleId)
    }

    override fun setTitle(title: CharSequence?) {
        titleTv.text = title
    }

    protected open fun canBack() = true
}