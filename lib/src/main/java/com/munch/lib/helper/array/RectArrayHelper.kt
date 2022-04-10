@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper.array

import android.graphics.Rect
import android.graphics.RectF

/**
 * 用array来替代大量的[android.graphics.Rect]对象
 *
 * Create by munch1182 on 2021/1/10 1:32.
 */
class RectFArrayHelper : SpecialFloatArrayHelper(4) {

    fun getLeft(index: Int) = getVal(index, 0)
    fun getTop(index: Int) = getVal(index, 1)
    fun getRight(index: Int) = getVal(index, 2)
    fun getBottom(index: Int) = getVal(index, 3)

    fun getWidth(index: Int) = getRight(index) - getLeft(index)
    fun getHeight(index: Int) = getBottom(index) - getTop(index)

    fun getCenterX(index: Int) = getRight(index) / 2 + getLeft(index) / 2
    fun getCenterY(index: Int) = getBottom(index) / 2f + getTop(index) / 2

    fun getRect(index: Int) =
        RectF(getLeft(index), getTop(index), getRight(index), getBottom(index))
}

class RectArrayHelper : SpecialIntArrayHelper(4) {

    fun getLeft(index: Int) = getVal(index, 0)
    fun getTop(index: Int) = getVal(index, 1)
    fun getRight(index: Int) = getVal(index, 2)
    fun getBottom(index: Int) = getVal(index, 3)

    fun getWidth(index: Int) = getRight(index) - getLeft(index)
    fun getHeight(index: Int) = getBottom(index) - getTop(index)

    fun getCenterX(index: Int) = getRight(index) / 2 + getLeft(index) / 2
    fun getCenterY(index: Int) = getBottom(index) / 2f + getTop(index) / 2

    fun getRect(index: Int) =
        Rect(getLeft(index), getTop(index), getRight(index), getBottom(index))
}