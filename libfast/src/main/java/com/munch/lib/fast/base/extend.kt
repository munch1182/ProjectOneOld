package com.munch.lib.fast.base

import android.app.Activity
import android.util.ArrayMap
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.*

/**
 * Create by munch1182 on 2021/8/9 15:00.
 */

/**
 * @see BaseDBFragment.bind
 */
inline fun <reified VDB : ViewDataBinding> Activity.bind(@LayoutRes layoutId: Int): Lazy<VDB> {
    return lazy { DataBindingUtil.setContentView(this, layoutId) }
}

/**
 * @see getInScope
 */
fun <VM : ViewModel> ViewModelStoreOwner.get(target: Class<VM>): Lazy<VM> {
    return lazy { ViewModelProvider(this).get(target) }
}

/**
 * 在[scope]作用域内共享同一个[ViewModel]
 * 如果[ViewModel]不被任何对象持有，则此vm会被回收
 *
 * 可以将scope通过注解的方式设置，但是没必要
 *
 * @see scope 自定义的作用域，第一个使用此作用域会得到一个新建的[target]对象，之后则会得到同一个[target]对象
 *
 * @see get
 */
fun <VM : ViewModel> LifecycleOwner.getInScope(scope: String, target: Class<VM>): Lazy<VM> {
    return lazy {
        val store = if (vmStores.keys.contains(scope)) {
            vmStores[scope]!!
        } else {
            val s = VMStore()
            vmStores[scope] = s
            s
        }
        store.bindHost(this)
        ViewModelProvider(store, ViewModelProvider.NewInstanceFactory()).get(target)
    }
}

private val vmStores = ArrayMap<String, VMStore>()

class VMStore : ViewModelStoreOwner {

    private var vmStore: ViewModelStore? = null
    private val bindTargets = ArrayList<LifecycleOwner>()

    fun bindHost(host: LifecycleOwner) {
        if (!bindTargets.contains(host)) {
            bindTargets.add(host)
            clearWhenNoNeed(host)
        }
    }

    override fun getViewModelStore(): ViewModelStore {
        if (vmStore == null) {
            vmStore = ViewModelStore()
        }
        return vmStore!!
    }

    private fun clearWhenNoNeed(host: LifecycleOwner) {
        host.lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                bindTargets.remove(host)
                if (bindTargets.isEmpty()) {
                    vmStores.entries.find { it.value == this@VMStore }
                        ?.also {
                            vmStore?.clear()
                            vmStores.remove(it.key)
                        }
                }
            }
        })

    }
}