package com.munch.lib.base

import android.view.View

/**
 * Create by munch1182 on 2021/8/10 17:46.
 */
interface OnViewIndexClickListener : View.OnClickListener, View.OnLongClickListener {

    override fun onClick(v: View?) {
        val holder = v?.tag as? Int? ?: return
        onClick(v, holder)
    }

    override fun onLongClick(v: View?): Boolean {
        val holder = v?.tag as? Int? ?: return false
        return onLongClick(v, holder)
    }

    fun onClick(v: View?, pos: Int) {}
    fun onLongClick(v: View?, pos: Int): Boolean = false
}