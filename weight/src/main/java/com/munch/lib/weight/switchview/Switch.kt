package com.munch.lib.weight.switchview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Checkable
import com.munch.lib.extend.*
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.extend.icontext.getColorPrimary
import com.munch.lib.graphics.Shape
import com.munch.lib.weight.*

class Switch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes),
    ViewHelper by ViewHelperDefault, TouchHelper by TouchHelperDefault,
    Checkable, IContext, IColorView, ViewUpdateListener<Switch> {

    companion object {
        private const val WIDTH_DEF = 40f
        private const val HEIGHT_DEF = 24f
    }

    sealed class Direction : SealedClassToStringByName() {
        object Left2Right : Direction()
        object Right2Left : Direction()
    }

    sealed class CheckType : SealedClassToStringByName() {
        object Any : CheckType()
        object Click : CheckType()
        object Call : CheckType()
    }

    private val roundRectangle = Shape.RoundRectangle()
    private val circle = Shape.Circle()
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isCheck = false
    private val move = Move()
    private var onCheckListener: OnCheckListener? = null

    var checkFrom: CheckType = CheckType.Any
    var direction: Direction = Direction.Left2Right

    //中心圆心与圆角矩形的间距
    var checkPadding = 4f

    var checkBgColor = getColorPrimary()
    var checkCenterColor = Color.WHITE
    var uncheckBgColor = Color.parseColor("#e8e8ea")
    var uncheckCenterColor = Color.WHITE

    private val nowBgColor: Int
        get() = if (isCheck) checkBgColor else uncheckBgColor
    private val nowCenterColor: Int
        get() = if (isCheck) checkCenterColor else uncheckCenterColor

    init {
        paint.style = Paint.Style.FILL
        circlePaint.style = Paint.Style.FILL

        context.obtainStyledAttributes(attrs, R.styleable.Switch).apply {
            when (getInteger(R.styleable.Switch_switch_direction, 0)) {
                0 -> direction = Direction.Left2Right
                1 -> direction = Direction.Right2Left
            }
            when (getInteger(R.styleable.Switch_switch_checkFrom, 0)) {
                0 -> checkFrom = CheckType.Any
                1 -> checkFrom = CheckType.Click
                2 -> checkFrom = CheckType.Call
            }
            checkPadding = getDimension(R.styleable.Switch_switch_checkPadding, checkPadding)
            checkBgColor = getColor(R.styleable.Switch_switch_checkBgColor, checkBgColor)
            checkCenterColor =
                getColor(R.styleable.Switch_switch_checkCenterColor, checkCenterColor)
            uncheckBgColor = getColor(R.styleable.Switch_switch_uncheckBgColor, uncheckBgColor)
            uncheckCenterColor =
                getColor(R.styleable.Switch_switch_uncheckCenterColor, uncheckCenterColor)
            isCheck = getBoolean(R.styleable.Switch_android_checked, false)
            move.speed = getFloat(R.styleable.Switch_switch_speed, move.speed)
            if (0f <= move.speed || move.speed >= 1f) {
                move.speed = 0.1f
            }
        }.recycle()
    }

    override fun update(update: Switch.() -> Unit) {
        update.invoke(this)
        invalidate()
    }

    override fun setColor(color: Int) {
        update { checkBgColor = color }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        getDefaultOrSize(this, WIDTH_DEF, HEIGHT_DEF, widthMeasureSpec, heightMeasureSpec).apply {
            setMeasuredDimension(first, second)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewRect(this, w, h)
        roundRectangle.x = paddingLeft.toFloat()
        roundRectangle.y = paddingTop.toFloat()
        roundRectangle.width = (w - paddingHorizontal()).toFloat()
        roundRectangle.height = (h - paddingVertical()).toFloat()
        roundRectangle.radius = roundRectangle.height / 2f

        circle.radius = (roundRectangle.height - checkPadding * 2f) / 2f
        val leftX = roundRectangle.x + checkPadding
        val rightX = w - paddingRight - checkPadding - circle.radius * 2f
        circle.x = when (direction) {
            Direction.Left2Right -> leftX
            Direction.Right2Left -> rightX
        }
        circle.y = roundRectangle.y + checkPadding
        //能移动的距离, 从一侧圆心到另一侧圆心
        move.width = roundRectangle.width - checkPadding * 2 - circle.radius * 2
        move.currX = circle.x

        move.offset = paddingLeft.toFloat() + checkPadding

        when (direction) {
            Direction.Left2Right -> move.progress = 0f
            Direction.Right2Left -> move.progress = 1f
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        drawBg(canvas)

        drawCenter(canvas)

        if (move.updateIfNeed()) {
            invalidate()
        } else {
            onCheckListener?.onCheck(isChecked)
        }
    }

    private fun drawCenter(canvas: Canvas) {
        circle.x = move.currX
        circlePaint.color = nowCenterColor
        circle.draw(canvas, circlePaint)
    }

    private fun drawBg(canvas: Canvas) {
        paint.color = nowBgColor
        roundRectangle.draw(canvas, paint)
    }

    override fun performClick(): Boolean {
        setChecked(!isChecked, true, CheckType.Click)
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        updateEvent(event)
        if (event.action == MotionEvent.ACTION_UP && isClick) {
            performClick()
        }
        return super.onTouchEvent(event)
    }

    override fun isChecked() = isCheck
    override fun toggle() {
        isChecked = !isChecked
    }

    override fun setChecked(checked: Boolean) {
        setChecked(checked, true)
    }

    fun setChecked(checked: Boolean, needAnim: Boolean) {
        setChecked(checked, needAnim, CheckType.Call)
    }

    private fun setChecked(checked: Boolean, needAnim: Boolean, from: CheckType) {
        if (checkFrom != CheckType.Any && checkFrom != from) {
            return
        }
        // 动画中的调用会被丢弃
        if (move.isAnim || isCheck == checked) return
        isCheck = checked
        move.performUpdate(needAnim)
        invalidate()
    }

    private data class Move(
        var width: Float = 0f,
        var speed: Float = 0.1f,
        var offset: Float = 0f
    ) {
        var progress = 0f
            set(value) {
                field = value
                currX = width * progress + offset
            }
        val isAnim: Boolean
            get() = progress != 0f && progress != 1f
        private var sign = 1
        var currX = 0f

        fun updateIfNeed(): Boolean {
            if (progress == 0f || progress == 1f) {
                return false
            } else if (progress < 0f) {
                progress = 0f
                return false
            } else if (progress > 1f) {
                progress = 1f
                return false
            }
            update()
            return true
        }

        fun performUpdate(needAnim: Boolean) {
            if (needAnim) {
                if (progress <= 0f) {
                    sign = 1
                } else if (progress >= 1f) {
                    sign = -1
                }
                update()
            } else {
                progress = if (progress == 0f) 1f else 0f
            }
        }

        private fun update() {
            progress += sign * speed
        }
    }

    fun setOnCheckListener(listener: OnCheckListener): Switch {
        onCheckListener = listener
        return this
    }

    fun interface OnCheckListener {
        fun onCheck(isCheck: Boolean)
    }
}