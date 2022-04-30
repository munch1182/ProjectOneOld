package com.munch.lib.fast.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import cn.munch.lib.DBRecord
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.munch.lib.AppHelper
import com.munch.lib.extend.getColorPrimary
import com.munch.lib.extend.inflate
import com.munch.lib.extend.inflateParent
import com.munch.lib.fast.measure.MeasureHelper
import com.munch.lib.fast.view.DispatcherActivity
import com.munch.lib.helper.BarHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Method

/**
 * Created by munch1182 on 2022/4/15 23:04.
 */
open class BaseFastActivity : DispatcherActivity() {

    private var measured = false

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        MeasureHelper.cost(MeasureHelper.KEY_LAUNCHER, 1000L) {
            lifecycleScope.launch(Dispatchers.Default) { DBRecord.time("launch cost $it ms") }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MeasureHelper.start(this::class.java.simpleName)
        onBar()
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
}