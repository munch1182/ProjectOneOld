package com.munch.lib.helper

import android.content.Context
import android.view.View

/**
 * Create by munch1182 on 2022/3/8 17:04.
 */
typealias ViewCreator = (context: Context) -> View

/**
 * 注意可能会重复调用的情形
 */
fun View.addPadding(l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0) {
    setPadding(l + paddingLeft, t + paddingTop, r + paddingRight, b + paddingBottom)
}