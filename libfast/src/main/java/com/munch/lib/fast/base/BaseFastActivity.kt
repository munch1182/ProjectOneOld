package com.munch.lib.fast.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.munch.lib.AppHelper
import com.munch.lib.extend.getColorPrimary
import com.munch.lib.fast.view.DispatcherActivity
import com.munch.lib.helper.BarHelper
import java.lang.reflect.Method

/**
 * Created by munch1182 on 2022/4/15 23:04.
 */
open class BaseFastActivity : DispatcherActivity() {

    protected val contentView: FrameLayout by lazy { findViewById(android.R.id.content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBar()
    }

    fun toast(str: CharSequence) {
        runOnUiThread { Toast.makeText(AppHelper.app, str, Toast.LENGTH_SHORT).show() }
    }

    protected open fun onBar() {
        BarHelper(this).colorStatusBar(getColorPrimary())
    }
}


open class BindBottomSheetDialogFragment : BottomSheetDialogFragment() {

    var viewBinding: ViewBinding? = null
        private set

    protected var method: Method? = null

    @Suppress("UNCHECKED_CAST")
    protected inline fun <reified VB : ViewBinding> bind(): Lazy<VB> {
        method = VB::class.java.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        return lazy { viewBinding as VB }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return bindView(inflater, container)
            ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    protected open fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        viewBinding?.root ?: method?.let { m ->
            try {
                m.isAccessible = true
                (m.invoke(null, inflater, container, false) as? ViewBinding)?.let {
                    viewBinding = it
                    it.root
                }
            } catch (e: Exception) {
                null
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}