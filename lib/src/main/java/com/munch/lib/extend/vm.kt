package com.munch.lib.extend

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*

/**
 * Create by munch1182 on 2022/4/14 17:13.
 */

val vmStores = hashMapOf<String, VmStore>()

class VmStore : ViewModelStoreOwner {

    private val binds = arrayListOf<LifecycleOwner>()
    private val vmStore by lazy { ViewModelStore() }

    override fun getViewModelStore() = vmStore

    fun register(host: LifecycleOwner) {
        if (binds.contains(host)) {
            return
        }
        binds.add(host)
        host.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                host.lifecycle.removeObserver(this)
                binds.remove(host)
                //在没有对象持有vm时销毁vm
                if (binds.isEmpty()) {
                    vmStores.entries.find { it.value == this@VmStore }
                        ?.also {
                            vmStore.clear()
                            vmStores.remove(it.key)
                        }
                }
            }
        })
    }
}

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
inline fun <reified VM : ViewModel> ComponentActivity.get(scopeName: String): Lazy<VM> {
    val vmStore = vmStores.getOrDefault(scopeName, null)
        ?: VmStore().apply { vmStores[scopeName] = this }
    vmStore.register(this)
    return lazy { ViewModelProvider(vmStore).get(VM::class.java) }
}

inline fun <reified VM : ViewModel> ViewModelStoreOwner.get(): Lazy<VM> {
    return lazy { ViewModelProvider(this).get(VM::class.java) }
}

inline fun <reified VM : ViewModel> Fragment.get(): Lazy<VM> {
    return lazy { ViewModelProvider(requireActivity()).get(VM::class.java) }
}