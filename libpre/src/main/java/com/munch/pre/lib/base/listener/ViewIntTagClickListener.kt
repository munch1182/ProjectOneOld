package com.munch.pre.lib.base.listener

import android.view.View

/**
 * 将view的tag设置为int时，可用此listener代替View.OnClickListener
 *
 * Create by munch1182 on 2021/3/31 10:40.
 */
interface ViewIntTagClickListener : View.OnClickListener {

    override fun onClick(v: View) {
        val index = v.tag as? Int ?: return
        onClick(v, index)
    }

    fun onClick(v: View, index: Int)
}

interface ViewIntTagLongClickListener : View.OnLongClickListener {

    override fun onLongClick(v: View): Boolean {
        val index = v.tag as? Int ?: return false
        return onLongClick(v, index)
    }

    fun onLongClick(v: View, index: Int): Boolean
}