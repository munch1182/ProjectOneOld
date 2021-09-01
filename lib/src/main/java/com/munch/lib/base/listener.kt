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

/**
 * 更正命名
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

interface OnTriggeredListener {
    fun onTriggered()
}

interface OnChangeListener {
    fun onChange()
}

interface OnResultListener<T> {
    fun onResult(result: T)
}

interface OnResultNullableListener<T> {
    fun onResult(result: T?)
}

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

interface OnCancelListener {
    fun onCancel()
}

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