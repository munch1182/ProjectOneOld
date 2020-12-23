package com.munch.lib.helper

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

/**
 * Create by munch1182 on 2020/12/7 10:27.
 */
/**
 * 给ViewGroup子控件批量设置点击事件
 * 注意：使用了view的tag
 * @param clazz 需要设置点击事件的子类类型
 */
fun ViewGroup.clickItem(listener: View.OnClickListener, vararg clazz: Class<out View>) {
    var index = 0
    var willTag: Boolean
    this.children.forEach view@{ view ->
        if (clazz.isNotEmpty()) {
            willTag = false
            clazz.forEach clazz@{ clazz ->
                //是需要的类
                if (clazz.isAssignableFrom(view::class.java)) {
                    willTag = true
                    return@clazz
                }
            }
            if (!willTag) {
                return@view
            }
        }
        view.tag = index
        view.setOnClickListener(listener)
        index++
    }
}

fun View.setMargin(margin: Int) = setMargin(margin, margin, margin, margin)
fun View.setMargin(lr: Int, tb: Int) = setMargin(lr, tb, lr, tb)

fun View.setMargin(l: Int, t: Int, r: Int, b: Int) {
    when (val params = this.layoutParams) {
        null -> {
            this.layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(l, t, r, b) }
        }
        is ViewGroup.MarginLayoutParams -> {
            params.setMargins(l, t, r, b)
        }
        else -> {
            this.layoutParams =
                ViewGroup.MarginLayoutParams(params).apply { setMargins(l, t, r, b) }
        }
    }
}
