package com.munch.lib.base

import android.view.View

/**
 * Created by munch1182 on 2022/4/9 4:23.
 */
interface OnUpdateListener {
    fun onUpdate()
}

typealias OnUpdate = () -> Unit
typealias OnReceive<T> = (received: T) -> Unit

interface OnResultListener<T> {
    fun onResult(result: T)
}

interface OnResultNullableListener<T> {
    fun onResult(result: T?)
}

interface OnNextListener {
    fun onNext()
}

typealias OnNext = () -> Unit

interface OnCancelListener {
    fun onCancel()
}

typealias OnCancel = () -> Unit

typealias OnShow = () -> Unit

interface Cancelable {
    fun cancel()
}

interface Destroyable {
    fun destroy()
}

interface Resettable {
    fun reset()
}

interface InitFunInterface {

    fun init()
}

interface Manageable : Cancelable, Destroyable

//<editor-fold desc="click">
@Suppress("UNCHECKED_CAST")
interface OnViewTagClickListener<T> : View.OnClickListener, View.OnLongClickListener {

    override fun onClick(v: View?) {
        val tagVal = v?.tag as? T? ?: return
        onClick(v, tagVal)
    }

    override fun onLongClick(v: View?): Boolean {
        val tagVal = v?.tag as? T? ?: return false
        return onLongClick(v, tagVal)
    }

    fun onClick(v: View?, tagVal: T) {}
    fun onLongClick(v: View?, tagVal: T): Boolean = false
}
typealias OnViewTagClick<T> = (v: View?, tag: T?) -> Unit
typealias OnViewTagLongClick<T> = (v: View?, tag: T?) -> Boolean

fun <T> OnViewTagClick<T>.toClickListener(): OnViewTagClickListener<T> {
    return object : OnViewTagClickListener<T> {
        override fun onClick(v: View?, tagVal: T) {
            super.onClick(v, tagVal)
            invoke(v, tagVal)
        }
    }
}

fun <T> OnViewTagLongClick<T>.toLongClickListener(): OnViewTagClickListener<T> {
    return object : OnViewTagClickListener<T> {
        override fun onLongClick(v: View?, tagVal: T): Boolean {
            return invoke(v, tagVal)
        }
    }
}

//</editor-fold>