package com.munch.project.launcher.base

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.munch.lib.helper.BarHelper
import com.munch.lib.helper.PhoneHelper
import com.munch.project.launcher.help.LoadViewHelper

/**
 * Create by munch1182 on 2021/2/23 14:43.
 */
open class BaseActivity : AppCompatActivity() {

    protected val contentView: FrameLayout by lazy { findViewById(android.R.id.content) }
    protected val barHelper by lazy { BarHelper(this) }
    protected val loadViewHelper by lazy { LoadViewHelper() }

    inline fun <reified T : ViewDataBinding> bind(layoutResID: Int): Lazy<T> = lazy {
        DataBindingUtil.setContentView(this, layoutResID)
    }

    inline fun <reified V : ViewModel> viewModel(): Lazy<V> = lazy {
        ViewModelProvider(this).get(V::class.java)
    }

    inline fun <reified V : ViewModel> viewModel(factory: ViewModelProvider.Factory): Lazy<V> =
        lazy {
            ViewModelProvider(this, factory).get(V::class.java)
        }

    //<editor-fold desc="setContentView">
    /**
     * [setContentView]的统一处理，统一将状态栏透明并将内容延申到状态栏
     */
    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        barHelper.hideStatusBar(true)
        super.setContentView(view, fitParams(params))
        setPage(view ?: return)
    }

    /**
     * 对页面统一设置，如果无需，则阻止其实现即可
     *
     * @param view contentView，可能未覆盖全页面
     */
    open fun setPage(view: View) {
        /*barHelper.colorStatusBar(Color.TRANSPARENT)*/
    }

    /**
     * 因为延申状态栏的缘故，将页面设置topMargin
     */
    open fun fitParams(params: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        val layoutParams = if (params != null) {
            FrameLayout.LayoutParams(params)
        } else {
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        layoutParams.topMargin += PhoneHelper.getStatusBarHeight()
        return layoutParams
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(View.inflate(this, layoutResID, null))
    }

    fun fullParams() = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    open fun setContentView(layoutResID: Int, params: ViewGroup.LayoutParams?) {
        setContentView(View.inflate(this, layoutResID, null), params)
    }

    override fun setContentView(view: View?) {
        setContentView(view, null)
    }
    //</editor-fold>
}