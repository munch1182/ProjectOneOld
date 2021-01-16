package com.munch.lib.test

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.google.android.material.appbar.MaterialToolbar
import com.munch.lib.helper.*

/**
 * 给其子类添加了一个[R.layout.activity_base_top]，不需要则不要继承此类，因为重写了[setContentView]
 *
 * 实际开发中此类应该继承app的BaseActivity
 *
 * Create by munch1182 on 2020/12/10 22:06.
 */
open class TestBaseTopActivity : BaseActivity() {

    private val topContainer by lazy { findViewById<ViewGroup>(R.id.top_container) }
    private val toolbar by lazy { findViewById<MaterialToolbar>(R.id.top_tool_bar) }

    open fun notShowBack() = false

    open fun showBack(show: Boolean = true) {
        if (show) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                toolbar.navigationIcon = getBackIconWhite()
            } else {
                toolbar.navigationIcon = getBackIcon()
            }
            toolbar.setNavigationOnClickListener { onBackPressed() }
        } else {
            toolbar.navigationIcon = null
            toolbar.setNavigationOnClickListener(null)
        }
    }

    override fun setTitle(title: CharSequence?) {
        /*super.setTitle(title)*/
        setTitle(title, getColorCompat(R.color.colorPrimary))
    }

    open fun hideTitle() {
        (toolbar.parent as View).visibility = View.GONE
    }

    open fun showTitle() {
        (toolbar.parent as View).visibility = View.VISIBLE
    }

    /**
     * 从设计上来说，toolbar的颜色设置应该固定在xml文件或者colorPrimary属性中
     *
     * 对于需要设置半透明或者透明之类的非本样式的设计，应该另起页面
     *
     * 对于开发项目来说，更建议自定义view而不是直接使用toolbar
     */
    open fun setTitle(title: CharSequence?, @ColorInt color: Int) {
        toolbar.title = title
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.setBackgroundColor(color)
    }

    open fun setTitle(@StringRes titleId: Int, @ColorInt color: Int) {
        setTitle(getString(titleId), color)
    }

    override fun setTitle(@StringRes titleId: Int) {
        /*super.setTitle(titleId)*/
        setTitle(titleId, getColorCompat(R.color.colorPrimary))
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        /*super.setContentView(layoutResID)*/
        setContentView(layoutResID, true)
    }

    open fun setContentView(@LayoutRes layoutResID: Int, fitTop: Boolean = true) {
        val contentView = View.inflate(this, R.layout.activity_base_top, null) as ViewGroup
        View.inflate(this, layoutResID, contentView)
        setView(contentView, fitTop)
    }

    open fun setView(view: View?, fitTop: Boolean = true) {
        super.setContentView(view?.apply {
            if (!fitTop) {
                return@apply
            }
            val container = this.findViewById<ViewGroup>(R.id.top_container) ?: return@apply
            container.getChildAt(1)?.apply pageView@{
                val actionBarSize = PhoneHelper.getActionBarSize().takeIf { it != -1 } ?: 0
                setMargin(
                    0, actionBarSize, 0, 0, true,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                setPageBg(this@pageView)
            }
        })

        title = this::class.java.simpleName.replace("Test", "").replace("Activity", "")
        showBack(!notShowBack())
    }

    /**
     *  给内容view设置背景，默认为白色，如果要更改或者不需要，组织其实现即可
     */
    open fun setPageBg(view: View) {
        view.setBackgroundColor(Color.WHITE)
    }

    open fun setContentView(view: View?, fitTop: Boolean = true) {
        val contentView = View.inflate(this, R.layout.activity_base_top, null) as ViewGroup
        contentView.addView(view)
        setView(contentView, fitTop)
    }

    override fun setContentView(view: View?) {
        /*super.setContentView(view)*/
        setContentView(view, true)
    }
}