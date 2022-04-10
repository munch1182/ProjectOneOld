package com.munch.lib.extend

import android.view.LayoutInflater
import androidx.core.app.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding

/**
 * Create by munch1182 on 2022/3/8 16:23.
 */
inline fun <reified VB : ViewBinding> ComponentActivity.bind(): Lazy<VB> {
    return lazy {
        try {
            val method = VB::class.java.getDeclaredMethod("inflate", LayoutInflater::class.java)
            val vb: VB = method.invoke(null, layoutInflater) as VB
            setContentView(vb.root)
            return@lazy vb
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

inline fun <reified VM : ViewModel> ViewModelStoreOwner.get(): Lazy<VM> {
    return lazy {
        ViewModelProvider(this).get(VM::class.java)
    }
}