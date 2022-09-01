package com.munch.lib.graphics

import android.graphics.PointF
import android.graphics.RectF

/**
 * Create by munch1182 on 2022/5/27 10:44.
 */
class RectF(l: Float, t: Float, r: Float, b: Float) : RectF(l, t, r, b) {

    constructor(rectF: RectF?) : this(
        rectF?.left ?: 0f,
        rectF?.top ?: 0f,
        rectF?.right ?: 0f,
        rectF?.bottom ?: 0f,
    )


    constructor() : this(0f, 0f, 0f, 0f)

    val centerX: Float
        get() = (left + right) / 2f
    val centerY: Float
        get() = (top + bottom) / 2f

    private var saveRect: RectF? = null

    /**
     * 用于存储当前的参数
     *
     * @see [restore]
     */
    fun save() {
        if (saveRect == null) {
            saveRect = RectF(this)
        } else {
            saveRect?.set(this)
        }
    }

    /**
     * 用于还原之前存储的参数
     *
     * @see [save]
     */
    fun restore() {
        saveRect?.also { set(it) }
    }

    /**
     * 判断[pointF]是否在这个rect的范围内
     */
    operator fun contains(pointF: PointF): Boolean {
        return contains(pointF.x, pointF.y)
    }
}