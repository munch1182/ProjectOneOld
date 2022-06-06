@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.*
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

/**
 * 获取屏幕宽高
 *
 * @param full 宽高是否包括系统栏区域，如状态栏、导航栏
 */
@Suppress("DEPRECATION")
fun WindowManager.getScreenSize(full: Boolean = false): Size {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = currentWindowMetrics
        val bd = metrics.bounds
        if (full) {
            Size(bd.width(), bd.height())
        } else {
            val mask = WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(mask)
            Size(
                (bd.width() - insets.right - insets.left),
                (bd.height() - insets.top - insets.bottom)
            )
        }
    } else {
        DisplayMetrics()
            .also { if (full) defaultDisplay.getRealMetrics(it) else defaultDisplay.getMetrics(it) }
            .let { Size(it.widthPixels, it.heightPixels) }
    }
}

inline fun Activity.getScreenSize(full: Boolean = false) = windowManager.getScreenSize(full)

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