@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper.array

/**
 * 用array来替代大量的[android.graphics.Rect]对象
 *
 * Create by munch1182 on 2021/1/10 1:32.
 */
class RectFArrayHelper : SpecialFloatArrayHelper(4) {

    fun getLeft(level: Int) = getVal(level, 0)
    fun getTop(level: Int) = getVal(level, 1)
    fun getRight(level: Int) = getVal(level, 2)
    fun getBottom(level: Int) = getVal(level, 3)

    fun getWidth(level: Int) = getRight(level) - getLeft(level)
    fun getHeight(level: Int) = getBottom(level) - getTop(level)

    fun getCenterX(level: Int) = getRight(level) / 2 + getLeft(level) / 2
    fun getCenterY(level: Int) = getBottom(level) / 2f + getTop(level) / 2
}

class RectArrayHelper : SpecialArrayHelper(4) {

    fun getLeft(level: Int) = getVal(level, 0)
    fun getTop(level: Int) = getVal(level, 1)
    fun getRight(level: Int) = getVal(level, 2)
    fun getBottom(level: Int) = getVal(level, 3)

    fun getWidth(level: Int) = getRight(level) - getLeft(level)
    fun getHeight(level: Int) = getBottom(level) - getTop(level)

    fun getCenterX(level: Int) = getRight(level) / 2 + getLeft(level) / 2
    fun getCenterY(level: Int) = getBottom(level) / 2f + getTop(level) / 2
}