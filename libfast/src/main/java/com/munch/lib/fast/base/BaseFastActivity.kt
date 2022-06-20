package com.munch.lib.fast.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.munch.lib.AppHelper
import com.munch.lib.DBRecord
import com.munch.lib.extend.*
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.fast.R
import com.munch.lib.fast.measure.MeasureHelper
import com.munch.lib.fast.view.DispatcherActivity
import com.munch.lib.helper.BarHelper
import com.munch.lib.helper.SkinHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Method

/**
 * Created by munch1182 on 2022/4/15 23:04.
 */
open class BaseFastActivity : DispatcherActivity(), IContext {

    private var measured = false
    protected open val bar by lazy { BarHelper(this) }
    protected open val skin by lazy { SkinHelper() }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        MeasureHelper.cost(MeasureHelper.KEY_LAUNCHER, 1000L) {
            lifecycleScope.launch(Dispatchers.Default) { DBRecord.time("launch cost $it ms") }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        skin.apply(this)
        super.onCreate(savedInstanceState)
        MeasureHelper.start(this::class.java.simpleName)
        onBar()
        skinUpdate()
    }

    private fun skinUpdate() {
        skin.onUpdate {
            val primary = SkinHelper.getColor(ctx, R.color.colorPrimary)
            val onPrimary = SkinHelper.getColor(ctx, R.color.colorOnPrimary)
            supportActionBar?.apply {
                setBackgroundDrawable(ColorDrawable(primary))
                title = title?.color(onPrimary)
                val home =
                    getAttrArrayFromTheme(android.R.attr.homeAsUpIndicator)
                    { getDrawable(0)?.apply { setTint(onPrimary) } }
                setHomeAsUpIndicator(home)
            }
            bar.colorStatusBar(primary)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!measured && hasFocus) {
            measured = true
            MeasureHelper.cost(this::class.java.simpleName, MeasureHelper.activityMeasureTime) {
                lifecycleScope.launch(Dispatchers.IO) {
                    DBRecord.time("${this@BaseFastActivity::class.java.simpleName} onCreate ~ onWindowFocusChanged cost $it ms")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        measured = true
    }

    fun toast(str: CharSequence) {
        runOnUiThread { Toast.makeText(AppHelper.app, str, Toast.LENGTH_SHORT).show() }
    }

    protected open fun onBar() {
        bar.colorStatusBar(getColorPrimary())
    }

    protected open fun addRightText(str: CharSequence, click: (View) -> Unit) {
        val v = TextView(this)
        v.background = getSelectableItemBackgroundBorderless()
        v.setTextColor(Color.WHITE)
        v.text = str
        v.setOnClickListener(click)
        addRight(v)
    }

    protected open fun addRight(v: View) {
        supportActionBar?.apply {
            val lp = ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT
            )
            lp.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setCustomView(v, lp)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            setDisplayShowCustomEnabled(true)
        }
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        lifecycleScope.launch(Dispatchers.Default) { compatViewSkin(view) }
        super.setContentView(view, params)
    }

    private fun compatViewSkin(view: View?) {
        view ?: return
        if (view is ViewGroup) {
            view.forEach { compatViewSkin(it) }
        } else {
            if (view is Button) {
                skin.add(
                    view,
                    mutableSetOf(SkinHelper.SkinAttr.BackgroundTint(R.color.colorPrimary))
                )
            }
            if (view is TextView) {
                skin.add(
                    view,
                    mutableSetOf(SkinHelper.SkinAttr.TextColor(R.color.colorText))
                )
            }

        }
    }
}


open class BindBottomSheetDialogFragment : BottomSheetDialogFragment(), IContext {

    var viewBinding: ViewBinding? = null
        private set

    protected var method: Method? = null

    protected inline fun <reified VB : ViewBinding> bind(): Lazy<VB> {
        method = VB::class.inflateParent()
        return lazy { viewBinding as VB }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return viewBinding?.root
            ?: inflaterView(inflater, container)
            ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    protected open fun inflaterView(inflater: LayoutInflater, container: ViewGroup?) =
        method?.inflate(inflater, container, false).apply { viewBinding = this }?.root

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override val ctx: Context
        get() = requireContext()
}