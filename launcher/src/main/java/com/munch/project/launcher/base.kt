package com.munch.project.launcher

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.munch.lib.android.helper.BarHelper
import com.munch.project.launcher.databinding.ActivityMainBinding

/**
 * Create by munch1182 on 2022/4/3 16:35.
 */
open class BaseActivity : AppCompatActivity() {

    protected val barHelper by lazy { BarHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barHelper.colorStatusBar(Color.TRANSPARENT)
    }
}

open class BaseFragment : Fragment() {

    protected var viewVB: ViewBinding? = null
    protected var viewClazz: Class<ViewBinding>? = null
    protected var destroyView = false

    protected inline fun <reified VB : ViewBinding> bind(destroyView: Boolean = false): Lazy<VB> {
        @Suppress("UNCHECKED_CAST")
        viewClazz = VB::class.java as Class<ViewBinding>
        this.destroyView = destroyView
        return lazy { viewVB as VB }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewVB ?: viewClazz?.let {
            val method = viewClazz?.getDeclaredMethod("inflate", LayoutInflater::class.java)
            viewVB = method?.invoke(null, inflater) as? ViewBinding
        }
        return viewVB?.root ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (destroyView) {
            viewVB = null
        }
    }
}