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

    protected open fun <VB : ViewBinding> bind(@LayoutRes layoutId: Int): Lazy<VB> {
        this.resId = layoutId
        @Suppress("UNCHECKED_CAST")
        return lazy { vdb as VB }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
    }
}