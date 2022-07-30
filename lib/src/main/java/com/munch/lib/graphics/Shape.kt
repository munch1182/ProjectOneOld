package com.munch.lib.graphics

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import com.munch.lib.extend.dis
import com.munch.lib.extend.isIn

sealed class Shape {

    open class Circle : Shape() {

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

        var x = 0f
            set(value) {
                field = value
                update()
            }
        var y = 0f
            set(value) {
                field = value
                update()
            }
        var radius = 0f
            set(value) {
                field = value
                update()
            }

        val centerPoint = PointF()

        protected open fun update() {
            centerPoint.set(x + radius, y + radius)
        }

        override fun draw(canvas: Canvas, paint: Paint) {
            canvas.drawCircle(centerPoint.x, centerPoint.y, radius, paint)
        }

        operator fun contains(point: PointF): Boolean {
            return centerPoint.dis(point) <= radius
        }

        override fun toString() = "Circle($x, $y, radius=$radius)"
    }

    open class Rectangle : Shape() {
        companion object {

            /**
             * 圆形
             *
             * @param x 左上点x
             * @param y 左上点y
             * @param width 宽
             * @param height 高
             */
            fun of(x: Float, y: Float, width: Float, height: Float): Rectangle {
                return Rectangle().apply {
                    this.x = x
                    this.y = y
                    this.width = width
                    this.height = height
                }
            }
        }

        var x = 0f
            set(value) {
                field = value
                update()
            }
        var y = 0f
            set(value) {
                field = value
                update()
            }
        var width = 0f
            set(value) {
                field = value
                update()
            }
        var height = 0f
            set(value) {
                field = value
                update()
            }

        var rect = RectF()

        protected open fun update() {
            rect.set(x, y, x + width, y + height)
        }

        override fun draw(canvas: Canvas, paint: Paint) {
            canvas.drawRect(rect, paint)
        }

        operator fun contains(point: PointF) = point.isIn(rect)

        override fun toString() = "Rectangle($x, $y, width=$width, height=$height)"
    }

    class Square : Rectangle() {

        companion object {
            fun of(x: Float, y: Float, size: Float) = Square().apply {
                this.x = x
                this.y = y
                this.width = size
                this.height = size
            }
        }

        override fun toString() = "Square($x, $y, size=$width)"
    }

    /**
     * 圆角矩形
     */
    open class RoundRectangle : Rectangle() {

        companion object {

            fun of(
                x: Float = 0f, y: Float = 0f,
                width: Float = 0f,
                height: Float = 0f,
                radius: Float = height / 2f
            ): RoundRectangle {
                return RoundRectangle().apply {
                    this.x = x
                    this.y = y
                    this.width = width
                    this.height = height
                    this.radius = radius
                }
            }
        }

        //矩形圆角半径
        var radius: Float = height / 2f

        override fun draw(canvas: Canvas, paint: Paint) {
            canvas.drawRoundRect(rect, radius, radius, paint)
        }

    }

    abstract fun draw(canvas: Canvas, paint: Paint)
}