@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method

/**
 * Create by munch1182 on 2022/3/8 16:23.
 */

inline fun ViewBinding.init() {
    this.apply { //nothing
    }
}

inline fun <reified VB : ViewBinding> ComponentActivity.bind(): Lazy<VB> {
    return lazy {
        try {
            val method = VB::class.java.getDeclaredMethod("inflate", LayoutInflater::class.java)
            method.isAccessible = true
            (method.invoke(null, layoutInflater) as? VB)!!.also {
                setContentView(it.root)
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

fun FragmentActivity.replace(id: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .replace(id, fragment)
        .commit()
}

open class BindFragment : Fragment() {

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
        container ?: return super.onCreateView(inflater, container, savedInstanceState)
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