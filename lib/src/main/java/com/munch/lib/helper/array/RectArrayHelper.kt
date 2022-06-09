@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper.array

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import com.munch.lib.graphics.RectF

/**
 * 用array来替代大量的[android.graphics.Rect]对象
 *
 * Create by munch1182 on 2021/1/10 1:32.
 */
class RectFArrayHelper : SpecialFloatArrayHelper(4), Iterable<RectF> {

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

    fun add(rectF: RectF) {
        add(rectF.left, rectF.top, rectF.right, rectF.bottom)
    }

    override fun iterator(): Iterator<RectF> = RectFIterator()

    private inner class RectFIterator : Iterator<RectF> {
        private val rectF = RectF()
        private var elementLine = 0


        override fun hasNext() = elementLine < size

        override fun next(): RectF {
            val index = elementLine
            rectF.set(getLeft(index), getTop(index), getRight(index), getBottom(index))
            elementLine++
            return rectF
        }
    }
}

class RectArrayHelper : SpecialIntArrayHelper(4), Iterable<Rect> {

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

    fun add(rect: Rect) {
        add(rect.left, rect.top, rect.right, rect.bottom)
    }

    override fun iterator(): Iterator<Rect> = RectIterator()

    private inner class RectIterator : Iterator<Rect> {
        private val rect = Rect()
        private var elementLine = 0

        override fun hasNext() = elementLine < size

        override fun next(): Rect {
            val index = elementLine
            rect.set(getLeft(index), getTop(index), getRight(index), getBottom(index))
            elementLine++
            return rect
        }
    }
}

class PointFArrayHelper : SpecialFloatArrayHelper(2), Iterable<PointF> {

    fun getX(index: Int) = getVal(index, 0)
    fun getY(index: Int) = getVal(index, 1)

    fun add(point: PointF) {
        add(point.x, point.y)
    }

    override fun iterator(): Iterator<PointF> = RectFIterator()

    private inner class RectFIterator : Iterator<PointF> {
        private val pointF = PointF()
        private var elementLine = 0


        override fun hasNext() = elementLine < size

        override fun next(): PointF {
            val index = elementLine
            pointF.set(getX(index), getY(index))
            elementLine++
            return pointF
        }
    }
}