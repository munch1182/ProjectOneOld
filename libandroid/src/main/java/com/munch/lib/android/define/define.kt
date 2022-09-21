package com.munch.lib.android.define

import android.content.Context
import android.view.View

/**
 * 声明一些常用函数别名
 */

/**
 * 用于创建一个View
 */
typealias ViewCreator = (Context) -> View

/**
 * 用于收到更新的通知
 */
typealias Notify = () -> Unit

/**
 * 用于收到有参数的更新
 */
typealias Receive<T> = (T) -> Unit

/**
 * 用于收到更新的通知
 */
typealias Update<T> = (T) -> Unit

/**
 * 交由外部传递一个view
 */
fun interface ViewProvider {
    fun setView(view: View)
}