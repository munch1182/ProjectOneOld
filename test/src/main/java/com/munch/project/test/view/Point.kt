package com.munch.project.test.view

/**
 * Create by munch1182 on 2021/1/9 15:58.
 */
data class Point @JvmOverloads constructor(
    var x: Float = UNSET,
    var y: Float = UNSET,
    private var unset: Float = UNSET
) {

    companion object {
        private const val UNSET = -1F
    }

    fun setUnset(unset: Float): Point {
        this.unset = unset
        return this
    }

    fun reset(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun clear(): Point {
        x = unset
        y = unset
        return this
    }

    fun isSet() = x != UNSET && y != UNSET
}