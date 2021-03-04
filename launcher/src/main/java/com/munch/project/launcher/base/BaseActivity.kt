package com.munch.project.launcher.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.munch.lib.helper.BarHelper
import com.munch.lib.helper.PhoneHelper
import com.munch.lib.helper.addPadding
import com.munch.project.launcher.help.LoadViewHelper

/**
 * Create by munch1182 on 2021/2/23 14:43.
 */
open class BaseActivity : AppCompatActivity() {

    protected val contentView: FrameLayout by lazy { findViewById(android.R.id.content) }
    protected val barHelper by lazy { BarHelper(this) }
    protected val loadViewHelper by lazy { LoadViewHelper() }
    protected val statusHeight = PhoneHelper.getStatusBarHeight()

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
        val fitParams = if (params != null) {
            FrameLayout.LayoutParams(params)
        } else {
            getParams()
        }
        fitStatus(view, fitParams)
        super.setContentView(view, fitParams)
        setPage(view ?: return)
    }

    /**
     * 对页面统一设置，如果无需，则阻止其实现即可
     *
     * @param view contentView，可能未覆盖全页面
     */
    open fun setPage(view: View) {
        barHelper.colorStatusBar(Color.TRANSPARENT)
    }

    /**
     * 因为延申状态栏的缘故，在此处设置适应
     */
    open fun fitStatus(contentView: View?, params: ViewGroup.LayoutParams?) {
        contentView?.addPadding(t = statusHeight)
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(View.inflate(this, layoutResID, null))
    }

    open fun getParams() = FrameLayout.LayoutParams(
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

abstract class BaseFragment<T : ViewDataBinding> : Fragment() {

    inline fun <reified V : ViewModel> viewModel(): Lazy<V> = lazy {
        ViewModelProvider(requireActivity()).get(V::class.java)
    }

    protected lateinit var bind: T
    protected abstract val resId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = DataBindingUtil.inflate(inflater, resId, container, false)
        bind.lifecycleOwner = this
        return bind.root
    }
}