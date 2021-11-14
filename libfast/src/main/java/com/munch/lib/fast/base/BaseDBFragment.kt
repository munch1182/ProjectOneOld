package com.munch.lib.fast.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.munch.lib.log.log

/**
 * 为[Fragment]添加[ViewDataBinding]的实现
 *
 * @see android.app.Activity.bind
 *
 * Create by munch1182 on 2021/8/9 15:48.
 */
open class BaseDBFragment : Fragment() {

    private var resId = 0
    private var vdb: ViewDataBinding? = null
    var vb: ViewBinding? = null

    protected open fun <VB : ViewDataBinding> bind(@LayoutRes layoutId: Int): Lazy<VB> {
        this.resId = layoutId
        @Suppress("UNCHECKED_CAST")
        return lazy { vdb as VB }
    }

    protected inline fun <reified VB : ViewBinding> bind(): Lazy<VB> {
        @Suppress("UNCHECKED_CAST")
        return lazy {
            try {
                val method = VB::class.java.getDeclaredMethod("inflate", LayoutInflater::class.java)
                method.isAccessible = true
                val vb: VB = method.invoke(null, layoutInflater) as VB
                this.vb = vb
                return@lazy vb
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (vb != null) {
            return vb?.root
        }
        if (vdb != null) {
            return vdb?.root
        }
        if (resId != 0) {
            vdb = DataBindingUtil.inflate(inflater, resId, container, false)
            vdb?.lifecycleOwner = this
            return vdb?.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vdb?.unbind()
        vdb = null
        vb = null
    }
}