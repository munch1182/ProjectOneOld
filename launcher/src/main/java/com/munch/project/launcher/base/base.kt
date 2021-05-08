package com.munch.project.launcher.base

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.munch.pre.lib.helper.BarHelper

/**
 * Create by munch1182 on 2021/5/8 10:23.
 */
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        toggleTheme()
        super.onCreate(savedInstanceState)
        BarHelper(this).hideStatusBar(true).colorStatusBar(Color.TRANSPARENT)
    }

    protected open fun toggleTheme() {
        SwitchHelper.INSTANCE.switchTheme(this)
    }
}

open class BaseFragment : Fragment() {

    private var resId: Int = 0
    private lateinit var vb: ViewDataBinding

    @Suppress("UNCHECKED_CAST")
    protected fun <T : ViewDataBinding> bind(@LayoutRes resId: Int): Lazy<T> {
        this.resId = resId
        return lazy {
            vb as T
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vb = DataBindingUtil.inflate(inflater, resId, container, false)
        vb.lifecycleOwner = this
        return vb.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vb.unbind()
    }
}