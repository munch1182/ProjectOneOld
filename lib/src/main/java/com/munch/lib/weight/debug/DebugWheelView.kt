package com.munch.lib.weight.debug

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import com.munch.lib.base.*
import com.munch.lib.log.log
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * Create by munch1182 on 2021/11/17 15:38.
 */
class DebugWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0
) : View(context, attrs, styleDef) {

    //<editor-fold desc="绘制部分">
    private var showCount = 3
    private val data = arrayOf("第一", "第2", "第三", "四", "第五个", "第六")

    private val paintSelect by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF272C31")
            textSize = context.sp2Px(24f)
        }
    }
    private val paintOther by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66272C31")
            textSize = context.sp2Px(20f)
        }
    }
    private val paintDebug by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            pathEffect = DashPathEffect(floatArrayOf(3f, 2f), 0f)
            style = Paint.Style.STROKE
        }
    }

    private val itemSpace by lazy { context.dp2Px(16f).toInt() }

    private val pointCenter = PointF()
    private val debugRect = RectF()
    private var textCenterHeight = 0
    private var textOtherHeight = 0

    //更新中心点需要移动的最小距离
    private var itemMoveHeight = 0f
    private var itemYDis = 0f
    private lateinit var debugMaxOther: Pair<Int, Int>
    private lateinit var debugMaxCenter: Pair<Int, Int>

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (data.isNotEmpty()) {
            val maxOther = paintOther.measureMaxTextSpace(data)
            val maxCenter = paintSelect.measureMaxTextSpace(data)
            debugMaxCenter = maxCenter
            debugMaxOther = maxOther
            val maxWidth = max(maxOther.first, maxCenter.first)
            textCenterHeight = maxCenter.second
            textOtherHeight = maxOther.second
            itemMoveHeight = textCenterHeight / 2f + itemSpace / 2f
            itemYDis = itemMoveHeight + itemSpace
            val minHeight = paddingTop + paddingBottom +
                    //非中心区域的文字总高
                    maxOther.second * (showCount - 1) +
                    //文字间间距的和
                    itemSpace * (showCount - 1) +
                    //中心文字高度
                    textCenterHeight +
                    //预留内部padding
                    4
            val minWidth = paddingLeft + paddingRight + maxWidth
            width = max(width, minWidth)
            height = max(height, minHeight)
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pointCenter.set(w / 2f, h / 2f)
    }

    private fun getStr(index: Int): String? {
        return data.getOrNull(index)
    }

    //</editor-fold>
    private var index = 2

    private var moveOffset = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //debug
        var cy = pointCenter.y
        canvas.drawLine(0f, cy, width.toFloat(), cy, paintDebug)
        //center
        cy += moveOffset
        var centerY = cy
        var left = pointCenter.x - debugMaxCenter.first / 2
        var top = centerY - debugMaxCenter.second / 2
        var right = pointCenter.x + debugMaxCenter.first / 2
        var bottom = centerY + debugMaxCenter.second / 2
        debugRect.set(left, top, right, bottom)
        canvas.drawRect(debugRect, paintDebug)
        canvas.drawLine(left, centerY, right, centerY, paintDebug)

        getStr(index + 1)?.let {
            //next
            centerY = cy + itemSpace + textCenterHeight / 2 + textOtherHeight / 2
            left = pointCenter.x - debugMaxOther.first / 2
            top = centerY - debugMaxOther.second / 2
            right = pointCenter.x + debugMaxOther.first / 2
            bottom = centerY + debugMaxOther.second / 2
            debugRect.set(left, top, right, bottom)
            canvas.drawRect(debugRect, paintDebug)
            canvas.drawLine(left, centerY, right, centerY, paintDebug)
        }

        getStr(index - 1)?.let {
            //last
            centerY = cy - itemSpace - textCenterHeight / 2 - textOtherHeight / 2
            left = pointCenter.x - debugMaxOther.first / 2
            top = centerY - debugMaxOther.second / 2
            right = pointCenter.x + debugMaxOther.first / 2
            bottom = centerY + debugMaxOther.second / 2
            debugRect.set(left, top, right, bottom)
            canvas.drawRect(debugRect, paintDebug)
            canvas.drawLine(left, centerY, right, centerY, paintDebug)
        }

        var y: Float
        getStr(index)?.let {
            y = cy
            canvas.drawTextInCenter(it, pointCenter.x, y, paintSelect)
        }
        repeat(showCount) { i ->
            val number = i + 1
            getStr(index + number)?.let {
                y = cy + itemSpace * number + (textCenterHeight / 2 + textOtherHeight / 2) * number
                if (y >= height + textOtherHeight / 2) {
                    return@repeat
                }
                canvas.drawTextInCenter(it, pointCenter.x, y, paintOther)
            } ?: return@repeat
        }
        repeat(showCount) { i ->
            val number = i + 1
            getStr(index - number)?.let {
                y = cy - itemSpace * number - (textCenterHeight / 2 + textOtherHeight / 2) * number
                if (y <= -textOtherHeight / 2) {
                    return@repeat
                }
                canvas.drawTextInCenter(it, pointCenter.x, y, paintOther)
            } ?: return@repeat
        }
    }

    private fun moveView(y: Float) {
        //边界
        /*if ((index == 0 && y > 0) || (index == data.size - 1 && y < 0)) {
            return
        }*/
        moveOffset += y
        if (moveOffset.absoluteValue > itemMoveHeight) {
            moveOffset = if (moveOffset > 0) {
                index--
                -itemMoveHeight
            } else {
                index++
                itemMoveHeight
            }
        }
        invalidate()
    }

    //<editor-fold desc="move部分">
    private val downPoint = PointF()
    private val lastPoint = PointF()

    private val vc = ViewConfiguration.get(context)

    //区分滑动的最小距离，低于这个距离不认为是滑动
    private val touchSlop = vc.scaledTouchSlop

    private val maxFlingVelocity = 4500
    private val minFlingVelocity = vc.scaledMinimumFlingVelocity

    //惯性滑动
    private var vt: VelocityTracker? = null
    private val flingHelper = FlingHelper()

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downPoint.set(event.x, event.y)
                lastPoint.set(event.x, event.y)
                flingHelper.stopFling()
                requestTouchEvent(true)
            }
            MotionEvent.ACTION_UP -> {
                //因为只做了竖方向的，竖方向几乎无移动则视为点击
                //此处未判断时间和长按事件
                if ((event.y.absoluteValue - downPoint.y.absoluteValue).absoluteValue < touchSlop) {
                    performClick()
                }
                requestTouchEvent(false)
            }
            MotionEvent.ACTION_MOVE -> {
                move(event.y - lastPoint.y)
                lastPoint.set(event.x, event.y)
            }
            MotionEvent.ACTION_CANCEL -> {
                log("ACTION_CANCEL")
            }
        }
        handTracker(event)
        return true
    }

    private fun handTracker(event: MotionEvent) {
        if (vt == null) {
            vt = VelocityTracker.obtain()
        }
        vt?.apply {
            addMovement(event)
            if (event.action == MotionEvent.ACTION_UP) {
                computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                var yVel = -yVelocity.toInt()
                yVel = if (yVel.absoluteValue < minFlingVelocity) 0 else
                    max(min(maxFlingVelocity, yVel), yVel)
                if (yVel == 0) {
                    fixEnd()
                } else {
                    flingHelper.fling(yVel)
                }
            }
        }
    }

    private fun move(y: Float) {
        moveView(y)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun fixEnd() {
        log("fixEnd:$moveOffset")
    }

    private fun onFling(dY: Int) {
        move(-dY.toFloat())
    }

    private fun onFlingEnd() {
        fixEnd()
    }

    private inner class FlingHelper : Runnable {

        private val scroller = OverScroller(context)
        private var lastY = 0

        fun fling(yVel: Int) {
            lastY = 0
            val minY = -(itemYDis * index + 1).toInt()
            val maxY = (itemYDis * (data.size - index + 1)).toInt()
            scroller.fling(
                0, 0, 0, yVel,
                0, 0, minY, maxY
            )
            postOnAnimation()
        }

        fun stopFling() {
            removeCallbacks(this)
            scroller.abortAnimation()
        }

        private fun postOnAnimation() {
            removeCallbacks(this)
            ViewCompat.postOnAnimation(this@DebugWheelView, this)
        }

        override fun run() {
            if (scroller.computeScrollOffset()) {
                val currY = scroller.currY
                val dY = currY - lastY
                lastY = currY
                onFling(dY)
                postOnAnimation()
            } else {
                onFlingEnd()
            }
        }

    }
    //</editor-fold>

}