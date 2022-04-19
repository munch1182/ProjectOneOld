@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * Create by munch1182 on 2022/3/8 16:23.
 */

inline fun ViewBinding.init() {
    this.apply { //nothing
    }
}

inline fun <reified VB : ViewBinding> KClass<VB>.inflateParent(): Method? =
    java.getDeclaredMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )

inline fun Method.inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    attach: Boolean
): ViewBinding? {
    return try {
        invoke(null, inflater, container, attach) as? ViewBinding
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

inline fun <reified VB : ViewBinding> KClass<VB>.inflate(): Method? =
    java.getDeclaredMethod("inflate", LayoutInflater::class.java)

inline fun Method.inflate(
    inflater: LayoutInflater,
): ViewBinding? {
    return try {
        invoke(null, inflater) as? ViewBinding
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

inline fun Activity.contentView(): FrameLayout = findViewById(android.R.id.content)

inline fun <reified VB : ViewBinding> ComponentActivity.bind(): Lazy<VB> {
    return lazy {
        VB::class.inflate()!!.inflate(layoutInflater)!!.also { v -> setContentView(v.root) } as VB
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
        method = VB::class.inflateParent()
        return lazy { viewBinding as VB }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container ?: return super.onCreateView(inflater, container, savedInstanceState)
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