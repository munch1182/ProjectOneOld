@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * Create by munch1182 on 2022/3/8 16:23.
 */
/**
 * 用于触发vb
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

inline fun Activity.contentView(): FrameLayout = findViewById(android.R.id.content)

inline fun <reified VB : ViewBinding> ComponentActivity.bind(): Lazy<VB> {
    return lazy {
        VB::class.inflate()!!.inflate(layoutInflater)!!.also { setContentView(it.root) } as VB
    }
}

fun FragmentActivity.replace(id: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .replace(id, fragment)
        .commit()
}

/**
 * 此方法只有在请求了一次权限之后才能使用，不能单独拿出来判断
 */
inline fun Activity.notDeniedForever(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
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