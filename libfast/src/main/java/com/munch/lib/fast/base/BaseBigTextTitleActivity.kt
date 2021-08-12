package com.munch.lib.fast.base

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.munch.lib.base.ViewHelper
import com.munch.lib.base.setDoubleClickListener
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
    protected open val ctlView: CollapsingToolbarLayout by lazy { findViewById(R.id.title_ctl_view) }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(R.layout.layout_big_text_title)

        findViewById<NestedScrollView>(R.id.title_scroll_view).apply {
            addView(view, params)
            scrollY = 0
        }
        findViewById<Toolbar>(R.id.title_tb_view).apply {
            if (canBack()) {
                setOnClickListener { onBackPressed() }
            } else {
                navigationIcon = null
            }
        }
        ctlView.setDoubleClickListener { showNotice() }

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
        title = getString(titleId)
    }

    override fun setTitle(title: CharSequence?) {
        //CollapsingToolbarLayout内设置了title，会自动添加与Toolbar的title的变化动画(是以绘制的方式)
        //详见com.google.android.material.appbar.CollapsingToolbarLayout的toolbarId和collapsingTextHelper参数以及onDraw方法
        ctlView.title = title
    }

    protected open fun canBack() = true

    protected open fun showNotice() {
        BottomSheetDialog(this, R.style.AppTheme_Dialog_Trans).show()
    }
}