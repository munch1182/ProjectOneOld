@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.app.Activity
import android.content.Intent
import android.view.Window
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass

/**
 * android.R.id.content
 */
inline val Activity.contentView: FrameLayout
    get() = findViewById(Window.ID_ANDROID_CONTENT)

/**
 * 使用ctx代替this, 用于一些多个this嵌套导致需要使用this@Activity的场景
 */
inline val Activity.ctx: Activity
    get() = this

/**
 * 使用反射在ComponentActivity中获取VB对象
 */
inline fun <reified VB : ViewBinding> Activity.bind(): Lazy<VB> {
    return lazy {
        VB::class.java.inflate(layoutInflater)!!.to<VB>().also { setContentView(it.root) }
    }
}

inline fun <reified VM : ViewModel> ComponentActivity.get(): Lazy<VM> {
    return lazy {
        ViewModelProvider(this, this.defaultViewModelProviderFactory)[VM::class.java]
    }
}

/**
 * 跳转到[T]
 */
inline fun <reified T : Activity> Activity.startActivity() = startActivity(T::class)

/**
 * 跳转到[target]
 */
inline fun Activity.startActivity(target: KClass<out Activity>) =
    startActivity(Intent(this, target.java))