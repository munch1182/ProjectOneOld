package com.munch.lib.test

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.appbar.MaterialToolbar
import com.munch.lib.helper.getBackIcon
import com.munch.lib.helper.getBackIconWhite

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base_top)
        title = this::class.java.simpleName.replace("Test", "").replace("Activity", "")

        showBack(!notShowBack())
    }

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
        toolbar.title = title
        toolbar.setTitleTextColor(Color.WHITE)
    }

    override fun setTitle(titleId: Int) {
        /*super.setTitle(titleId)*/
        title = getString(titleId)
    }

    override fun setContentView(layoutResID: Int) {
        /*super.setContentView(layoutResID)*/
        setContentView(LayoutInflater.from(this).inflate(layoutResID, topContainer, false))
    }

    override fun setContentView(view: View?) {
        /*super.setContentView(view)*/
        topContainer.addView(view)
    }
}