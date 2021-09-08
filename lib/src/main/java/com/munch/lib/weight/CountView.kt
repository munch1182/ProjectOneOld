package com.munch.lib.weight

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.munch.lib.R

/**
 * Create by munch1182 on 2021/4/8 17:16.
 */
class CountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var curCount = 0
    private var fontMetrics: Paint.FontMetrics
    private var lastCount = 0

    private var endCy = 0f
    private var textHeight = 0f

    /**
     * 数字切换时不变的部分
     */
    private var stableStr = ""
    private var stableStrWidth = 0f

    private var isAdd = false
    private var progress = 1f

    var max = Int.MAX_VALUE
    var min = Int.MIN_VALUE

    private val valueAnimator = ValueAnimator.ofFloat(0f, 100f)

    private var countChangeListener: ((count: Int) -> Unit)? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CountView)
        paint.color = typedArray.getColor(R.styleable.CountView_count_view_textColor, Color.GRAY)
        paint.textSize = typedArray.getDimension(R.styleable.CountView_count_view_textSize, 50f)
        curCount = typedArray.getInt(R.styleable.CountView_count_view_count, 0)
        max = typedArray.getInt(R.styleable.CountView_count_view_max, max)
        min = typedArray.getInt(R.styleable.CountView_count_view_min, min)
        typedArray.recycle()
        stableStr = curCount.toString()
        paint.style = Paint.Style.FILL
        fontMetrics = paint.fontMetrics
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        setMeasuredDimension(
            (paint.measureText(curCount.toString()) + paddingLeft + paddingRight).toInt(),
            (fontMetrics.bottom - fontMetrics.top + paddingTop + paddingBottom).toInt()
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        endCy = measuredHeight / 2 + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        textHeight = fontMetrics.descent - fontMetrics.ascent
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.alpha = 255

        canvas.drawText(stableStr, paddingLeft.toFloat(), endCy, paint)

        if (stableStr == curCount.toString()) {
            return
        }

        val lastCountCy: Float
        val curCountCy: Float
        if (isAdd) {
            lastCountCy = endCy - textHeight + textHeight * (1 + progress / 100)
            curCountCy = endCy - textHeight + textHeight * (progress / 100)
        } else {
            lastCountCy = endCy - textHeight + textHeight * (1 - progress / 100)
            curCountCy = endCy - textHeight + textHeight * (2 - progress / 100)
        }
        paint.alpha = (255f * (1f - progress / 100f)).toInt()
        canvas.drawText(
            getChangeStr(lastCount.toString()),
            paddingLeft.toFloat() + stableStrWidth,
            lastCountCy,
            paint
        )
        paint.alpha = (255f * progress / 100f).toInt()
        canvas.drawText(
            getChangeStr(curCount.toString()),
            paddingLeft.toFloat() + stableStrWidth,
            curCountCy,
            paint
        )
    }

    private fun getChangeStr(targetStr: String): String {
        return if (stableStr.length > targetStr.length) {
            targetStr
        } else {
            targetStr.subSequence(stableStr.length, targetStr.length)
                .toString()
        }
    }

    fun setCount(count: Int) {
        if (count < min || count > max || curCount == count) {
            return
        }
        lastCount = curCount
        curCount = count
        stableStr = curCount.toString()
        invalidate()
        countChangeListener?.invoke(curCount)
    }

    fun animSetCount(count: Int) {
        if (count < min || count > max || curCount == count) {
            return
        }
        lastCount = curCount
        curCount = count

        compareCount()

        val needLayout = lastCount.toString().length != curCount.toString().length
        //递增增位时需要在动画开始前重新测量大小
        if (needLayout && isAdd) {
            requestLayout()
        }

        valueAnimator.removeAllUpdateListeners()
        valueAnimator.addUpdateListener {
            val animatedValue = it.animatedValue
            if (animatedValue is Float) {
                progress = animatedValue
                invalidate()
            }
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                //递减缩位时需要在动画结束后重新测量大小
                if (needLayout && !isAdd) {
                    requestLayout()
                }
                valueAnimator.removeAllListeners()
                countChangeListener?.invoke(curCount)
            }
        })
        valueAnimator.start()
    }

    fun getCount() = curCount

    fun setCountChangeListener(func: ((count: Int) -> Unit)? = null): CountView {
        this.countChangeListener = func
        return this
    }

    private fun compareCount() {
        isAdd = curCount > lastCount
        stableStr = getStableStr(curCount.toString(), lastCount.toString())
        stableStrWidth = paint.measureText(stableStr)
    }

    private fun getStableStr(str1: String, str2: String): String {
        for (i in 0..str1.length.coerceAtMost(str2.length)) {
            if (str1[i] != str2[i]) {
                return str1.substring(0, i)
            }
        }
        return if (str1.length > str2.length) str2 else str1
    }

    fun countAdd() {
        animSetCount(curCount + 1)
    }

    fun countSub() {
        animSetCount(curCount - 1)
    }
}