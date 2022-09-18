package com.munch.lib.android.helper.array

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import com.munch.lib.android.extend.to
import kotlin.math.hypot

/**
 * Create by munch on 2022/9/17 6:56.
 */
class ArrayHelperIterator<N, ANY>(
    val helper: IterableArrayHelper<N, ANY>
) : Iterator<ANY> {
    private var currLevel = 0
    override fun hasNext(): Boolean = currLevel < helper.size
    override fun next(): ANY = helper.get(currLevel++, helper.tmp)
}

abstract class IterableArrayHelper<N, ANY> : SpecialArray<N>, Iterable<ANY> {
    abstract val tmp: ANY
    abstract fun get(level: Int, tmp: ANY): ANY
    override fun iterator(): Iterator<ANY> = ArrayHelperIterator(this)
}

//<editor-fold desc="rect">
abstract class BaseRectArrayHelper<N : Number, RECT> : IterableArrayHelper<N, RECT>() {
    override val unit: Int = 4
    override val array: MutableList<N> = mutableListOf()

    fun getLeft(level: Int) = getVal(level, 0)
    fun getTop(level: Int) = getVal(level, 1)
    fun getRight(level: Int) = getVal(level, 2)
    fun getBottom(level: Int) = getVal(level, 3)

    fun getWidth(level: Int): N = (getRight(level).toFloat() - getLeft(level).toFloat()).to()
    fun getHeight(level: Int): N = (getBottom(level).toFloat() - getTop(level).toFloat()).to()

    fun getCenterX(level: Int): Float =
        (getLeft(level).toFloat() + getRight(level).toFloat()) * 0.5f

    fun getCenterY(level: Int): Float =
        (getTop(level).toFloat() + getBottom(level).toFloat()) * 0.5f
}

class RectFArrayHelper(override val tmp: RectF = RectF()) : BaseRectArrayHelper<Float, RectF>() {
    override fun get(level: Int, tmp: RectF): RectF {
        tmp.set(getLeft(level), getTop(level), getRight(level), getBottom(level))
        return tmp
    }
}

class RectArrayHelper(override val tmp: Rect = Rect()) : BaseRectArrayHelper<Int, Rect>() {
    override fun get(level: Int, tmp: Rect): Rect {
        tmp.set(getLeft(level), getTop(level), getRight(level), getBottom(level))
        return tmp
    }
}
//</editor-fold>

//<editor-fold desc="point">
abstract class BasePointArrayHelper<N : Number, POINT> : IterableArrayHelper<N, POINT>() {
    override val unit: Int = 2
    override val array: MutableList<N> = mutableListOf()

    fun getX(level: Int) = getVal(level, 0)
    fun getY(level: Int) = getVal(level, 1)

    fun getLength(level: Int) = hypot(getX(level).toDouble(), getY(level).toDouble()).toFloat()
}

class PointFArrayHelper(override val tmp: PointF = PointF()) :
    BasePointArrayHelper<Float, PointF>() {
    override fun get(level: Int, tmp: PointF): PointF {
        tmp.set(getX(level), getY(level))
        return tmp
    }
}

class PointArrayHelper(override val tmp: Point = Point()) : BasePointArrayHelper<Int, Point>() {
    override fun get(level: Int, tmp: Point): Point {
        tmp.set(getX(level), getY(level))
        return tmp
    }
}
//</editor-fold>