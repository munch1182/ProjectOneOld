package com.munch.lib.base

import android.view.View

/**
 * Create by munch1182 on 2021/8/10 17:46.
 */
interface OnViewIntClickListener : View.OnClickListener, View.OnLongClickListener {

    override fun onClick(v: View?) {
        val intVal = v?.tag as? Int? ?: return
        onClick(v, intVal)
    }

    override fun onLongClick(v: View?): Boolean {
        val intVal = v?.tag as? Int? ?: return false
        return onLongClick(v, intVal)
    }

    fun onClick(v: View?, intVal: Int) {}
    fun onLongClick(v: View?, intVal: Int): Boolean = false
}

typealias OnViewIndexClick = (v: View?, index: Int) -> Unit
typealias OnViewIndexLongClick = (v: View?, index: Int) -> Boolean

fun OnViewIndexClick.toClickListener(): OnViewIndexClickListener {
    return object : OnViewIndexClickListener {
        override fun onClick(v: View?, index: Int) {
            super.onClick(v, index)
            invoke(v, index)
        }
    }
}

fun OnViewIndexLongClick.toLongClickListener(): OnViewIndexClickListener {
    return object : OnViewIndexClickListener {
        override fun onLongClick(v: View?, index: Int): Boolean {
            return invoke(v, index)
        }
    }
}

/**
 * 更正onClick的参数命名
 */
interface OnViewIndexClickListener : View.OnClickListener, View.OnLongClickListener {

    override fun onClick(v: View?) {
        val intVal = v?.tag as? Int? ?: return
        onClick(v, intVal)
    }

    override fun onLongClick(v: View?): Boolean {
        val holder = v?.tag as? Int? ?: return false
        return onLongClick(v, holder)
    }

    fun onClick(v: View?, index: Int) {}
    fun onLongClick(v: View?, index: Int): Boolean = false
}

interface OnChangeListener {
    fun onChange()
}

typealias OnChange = () -> Unit
typealias OnReceive<T> = (received: T) -> Unit

interface OnResultListener<T> {
    fun onResult(result: T)
}

interface OnResultNullableListener<T> {
    fun onResult(result: T?)
}

typealias OnIndexChange = (index: Int) -> Unit

interface OnConnectListener {
    fun onConnected()
    fun onDisconnected()
}

interface OnConnectValueListener<T> {
    fun onConnected(d: T)
    fun onDisconnected()
}

interface OnNextListener {
    fun onNext()
}

typealias OnNext = () -> Unit

interface OnCancelListener {
    fun onCancel()
}

typealias OnCancel = () -> Unit

interface Cancelable {
    fun cancel()
}

interface Destroyable {
    fun destroy()
}

interface Resettable {
    fun reset()
}

interface Manageable : Cancelable, Destroyable