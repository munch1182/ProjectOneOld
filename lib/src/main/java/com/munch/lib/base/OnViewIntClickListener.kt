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
        val holder = v?.tag as? Int? ?: return false
        return onLongClick(v, holder)
    }

    fun onClick(v: View?, intVal: Int) {}
    fun onLongClick(v: View?, intVal: Int): Boolean = false
}