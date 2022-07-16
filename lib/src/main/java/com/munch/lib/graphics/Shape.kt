package com.munch.lib.graphics

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import com.munch.lib.extend.dis

sealed class Shape {

    class Circle : Shape() {

        companion object {

            /**
             * 圆形
             *
             * @param x 左上点x
             * @param y 左上点y
             * @param radius 半径
             */
            fun of(x: Float, y: Float, radius: Float): Circle {
                return Circle().apply {
                    this.x = x
                    this.y = y
                    this.radius = radius
                }
            }
        }

        var x: Float = 0f
            set(value) {
                field = value
                update()
            }
        var y: Float = 0f
            set(value) {
                field = value
                update()
            }
        var radius: Float = 0f
            set(value) {
                field = value
                update()
            }

        private val centerPoint = PointF()

        private fun update() {
            centerPoint.set(x + radius, y + radius)
        }

        override fun draw(canvas: Canvas, paint: Paint) {
            canvas.drawCircle(centerPoint.x, centerPoint.y, radius, paint)
        }

        operator fun contains(point: PointF): Boolean {
            return centerPoint.dis(point) <= radius
        }
    }

    /**
     * 矩形
     *
     * @param x 左上点x
     * @param y 左上点y
     * @param width 长度
     * @param height 宽度
     */
    data class Rectangle(var x: Float, var y: Float, var width: Float, var height: Float) :
        Shape() {
        override fun draw(canvas: Canvas, paint: Paint) {
            canvas.drawRect(x, y, x + width, y + height, paint)
        }

        operator fun contains(point: PointF): Boolean {
            return point.x in x..x + width && point.y in y..y + height
        }
    }

    /**
     * 正方形
     *
     * @param x 左上点x
     * @param y 左上点y
     * @param size 长度
     */
    data class Square(var x: Float, var y: Float, var size: Float) : Shape() {
        override fun draw(canvas: Canvas, paint: Paint) {
            canvas.drawRect(x, y, x + size, y + size, paint)
        }

        operator fun contains(point: PointF): Boolean {
            return point.x in x..(x + size) && point.y in y..(y + size)
        }
    }


    abstract fun draw(canvas: Canvas, paint: Paint)
}