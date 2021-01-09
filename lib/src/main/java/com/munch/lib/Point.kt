package com.munch.lib

/**
 * Create by munch1182 on 2021/1/9 15:58.
 */
data class Point constructor(
    var x: Float = UNSET,
    var y: Float = UNSET,
    private var unset: Float = UNSET
) {

    companion object {
        const val UNSET = -1F
    }

    fun setUnset(unset: Float): Point {
        this.unset = unset
        return this
    }

    fun getUnset() = unset

    fun reset(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun reset(point: Point) {
        reset(point.x, point.y)
    }

    fun clear(): Point {
        x = getUnset()
        y = getUnset()
        return this
    }

    fun isSet() = x != getUnset() && y != getUnset()
}