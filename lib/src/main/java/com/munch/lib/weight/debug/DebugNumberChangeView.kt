package com.munch.lib.weight.debug

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.munch.lib.base.measureTextBounds

/**
 * Create by munch1182 on 2021/8/29 13:59.
 */
class DebugNumberChangeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, styleDef, defStyleRes) {

    /**
     * 使用集合来存放所有数字
     */
    var numbers: List<String>? = null

    /**
     *  使用回调来生成下一个/上一个数字
     */
    var numberGetter: ((now: String?, addOrReduce: Boolean) -> String)? = null

    /**
     * 设置数字的间隔，自动加减
     */
    var space: Number = 1

    var max: String = ""

    @ColorInt
    var textColor: Int = Color.BLACK
    var textSize: Float = 36f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        this.textSize = this@DebugNumberChangeView.textSize
    }

    private var nowIndex = 0
    private var nowNumber: String? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec)
            )
            return
        }
        paint.textSize = textSize
        sureMax()
        val rect = paint.measureTextBounds(max)
        val width = if (widthMode != MeasureSpec.EXACTLY) {
            rect.width()
        } else {
            MeasureSpec.getSize(widthMeasureSpec)
        }
        val height = if (heightMode != MeasureSpec.EXACTLY) {
            rect.height() * 3
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
    }

    private fun sureMax() {
        if (max.isBlank()) {
            if (numbers?.size ?: 0 > 0) {
                max = numbers?.lastOrNull() ?: ""
            }
        }
    }

    private fun getNumber(index: Int): String {
        return when {
            numbers != null -> numbers!![index]
            numberGetter != null -> numberGetter?.invoke(nowNumber, nowIndex < index)!!
            else -> {
                val isAdd = nowIndex < index
                val change = if (isAdd) space.toInt() else -space.toInt()
                when (space) {
                    is Int -> (nowNumber?.toInt() ?: 0) + change
                    is Long -> (nowNumber?.toLong() ?: 0L) + change
                    is Float -> (nowNumber?.toFloat() ?: 0f) + change
                    is Double -> (nowNumber?.toDouble() ?: 0.0) + change
                    else -> throw IllegalStateException()
                }.toString()
            }
        }
    }
}