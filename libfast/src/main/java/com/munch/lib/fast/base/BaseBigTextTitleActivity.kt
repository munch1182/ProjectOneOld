package com.munch.lib.fast.base

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.munch.lib.base.ViewHelper
import com.munch.lib.fast.R
import com.munch.lib.helper.BarHelper

/**
 * 用大文字当作标题的基类
 *
 * 当前不能使用SwipeRefreshLayout作为根布局
 *
 * Create by munch1182 on 2021/8/10 16:20.
 */
open class BaseBigTextTitleActivity : BaseActivity() {

    protected open val container: FrameLayout by lazy { findViewById(android.R.id.content) }
    protected open val titleTv: TextView by lazy { findViewById(R.id.title_tv) }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(R.layout.layout_big_text_title)

        findViewById<NestedScrollView>(R.id.title_scroll_view).apply {
            addView(view, params)
            scrollY = 0
        }
        findViewById<View>(R.id.title_back).apply {
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