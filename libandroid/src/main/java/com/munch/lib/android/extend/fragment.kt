package com.munch.lib.android.extend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method

/**
 * 给[Fragment]提供[bind]方法来提供一个[ViewBinding]对象
 */
open class BindFragment : Fragment() {

    protected open var viewBind: ViewBinding? = null
    protected var method: Method? = null

    protected inline fun <reified VB : ViewBinding> bind(): Lazy<VB> {
        method = VB::class.java.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
        )
        return lazy { viewBind!!.to() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return viewBind?.root
            ?: inflaterView(inflater, container)
            ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun inflaterView(inflater: LayoutInflater, container: ViewGroup?): View? {
        viewBind = method?.invoke(null, inflater, container, false)?.to()
        return viewBind?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBind = null
    }

}