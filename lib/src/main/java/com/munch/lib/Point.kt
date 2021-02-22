@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF


/**
 * Create by munch1182 on 2021/1/9 15:58.
 */


fun getUnset() = -1f

fun PointF.reset(x: Float = getUnset(), y: Float = getUnset()): PointF {
    this.x = x
    this.y = y
    return this
}

fun PointF.reset(point: PointF) {
    reset(point.x, point.y)
}

fun PointF.clear(): PointF {
    reset()
    return this
}

fun PointF.isSet() = x != getUnset() && y != getUnset()

fun Canvas.drawLines(paint: Paint, vararg point: PointF) {
    val floatArray = FloatArray(point.size * 2)
    point.forEachIndexed { index, pointF ->
        floatArray[index * 2] = pointF.x
        floatArray[index * 2 + 1] = pointF.y
    }
    drawLines(floatArray, paint)
}

fun Path.moveTo(point: PointF): Path {
    this.moveTo(point.x, point.y)
    return this
}

fun Path.quadTo(start: PointF, control: PointF, end: PointF): Path {
    this.moveTo(start.x, start.y)
    this.quadTo(control.x, control.y, end.x, end.y)
    return this
}

fun Path.quadTo(control: PointF, end: PointF): Path {
    this.quadTo(control.x, control.y, end.x, end.y)
    return this
}

fun Path.lineTo(point: PointF): Path {
    this.lineTo(point.x, point.y)
    return this
}

fun Path.lineTo(vararg point: PointF): Path {
    point.forEach {
        lineTo(it)
    }
    return this
}
