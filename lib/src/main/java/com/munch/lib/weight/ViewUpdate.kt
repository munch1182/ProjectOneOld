package com.munch.lib.weight

import android.view.View

/**
 * Create by munch1182 on 2021/8/14 15:24.
 */
interface ViewUpdate<T : View> {

    /**
     * 用于一次性更改多个属性，然后统一刷新
     * 具体刷新方法自定义view应自行调用[View.requestLayout]或者[View.invalidate]
     */
    @Suppress("UNCHECKED_CAST")
    fun set(set: T.() -> Unit) {
        set.invoke(this as T)
    }
}