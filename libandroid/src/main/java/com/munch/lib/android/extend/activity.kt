@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import android.app.Activity
import android.content.Intent
import android.view.Window
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.collection.ArrayMap
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding
import kotlin.collections.set
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

//<editor-fold desc="ShareViewModel">
/**
 * 获取一个ViewModel
 * 如果已有至少一个对象持有该scopeName的ViewModel，此时获取则会返回相同对象的ViewModel
 * 如果没有对象持有该scopeName的ViewModel，则该ViewModel会被销毁，下次获取时会重建
 *
 * 注意:
 * 1. 不同的scopeName，即使ViewModel类型相同，也会获取不同的对象
 * 2. 想要不同对象之间共享ViewModel，对象之间的生命周期至少要有重叠部分
 * 3. 同一scopeName获取不同的ViewModel，则旧的ViewModel会被clear并被覆盖
 */
inline fun <reified VM : ViewModel> LifecycleOwner.get(scopeName: String): Lazy<VM> {
    val vmStore = ShareVMHelper.register(this, scopeName) // 此时已经注册
    return lazy { ViewModelProvider(vmStore)[VM::class.java] }
}

object ShareVMHelper {
    // 生命周期同app
    internal val vmStores = ArrayMap<String, VMStore>()

    fun register(owner: LifecycleOwner, scopeName: String): VMStore {
        val vmStore = vmStores.getOrDefault(scopeName, VMStore(scopeName))
        vmStore.register(owner)
        return vmStore
    }

    class VMStore(private val scopeName: String) : ViewModelStoreOwner {

        private val vmStore by lazy { ViewModelStore() }
        private val binds = mutableListOf<LifecycleOwner>()

        override fun getViewModelStore() = vmStore

        fun register(owner: LifecycleOwner) {
            if (binds.contains(owner)) return // 如果已经添加

            vmStores[scopeName] = this // 保存以便跨单个生命周期使用

            binds.add(owner)

            owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    owner.lifecycle.removeObserver(this)

                    binds.remove(owner)
                    if (binds.isEmpty()) { // 如果不再被持有
                        vmStores.entries.find { it.value == this@VMStore }
                            ?.let {
                                vmStore.clear()
                                vmStores.remove(it.key) // 清除
                            }
                    }
                }
            })
        }
    }

}
//</editor-fold>