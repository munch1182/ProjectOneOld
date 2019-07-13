package com.munch.weight.switchbutton

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.munch.weight.R
import kotlin.math.roundToInt

/**
 * Created by Munch on 2019/7/12 9:09
 */
class SwitchButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr), View.OnClickListener {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var ballColor: Int = 0
    private var checkColor: Int = 0
    private var uncheckColor: Int = 0
    private val ballPaint: Paint by lazy(mode = LazyThreadSafetyMode.NONE) { Paint() }
    private val bgPaint: Paint by lazy(mode = LazyThreadSafetyMode.NONE) { Paint() }

    init {
        if (attrs != null) {
            val typedArray = getContext().theme?.obtainStyledAttributes(
                attrs,
                R.styleable.SwitchBottom,
                defStyleAttr,
                R.style.def_switch_button
            )

            if (typedArray != null) {
                ballColor = typedArray.getColor(R.styleable.SwitchBottom_switch_ball_color, Color.WHITE)
                uncheckColor = typedArray.getColor(R.styleable.SwitchBottom_switch_bg_uncheck_color, Color.GRAY)
                checkColor = typedArray.getColor(R.styleable.SwitchBottom_switch_bg_check_color, Color.BLUE)

                typedArray.recycle()
            }
        }
        initPaint()
        setOnClickListener(this)
    }

    private var viewHeight = 0
    private var viewWidth = 0
    private var switchViewStrokeWidth = 0
    private var strokeRadius = 0
    private var solidRadius = 0
    private var ballXRight = 0
    private var switchBallX = 0
    private lateinit var bgStrokeRectF: RectF
    private var isChecked = false

    private fun initPaint() {
        setPaint(ballPaint, ballColor)
        setPaint(bgPaint, uncheckColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewHeight = h
        viewWidth = w
        switchViewStrokeWidth = (w * 1f / 30).roundToInt()

        strokeRadius = viewHeight / 2
        solidRadius = (viewHeight - 2 * switchViewStrokeWidth) / 2

        ballXRight = viewWidth - solidRadius - switchViewStrokeWidth
        switchBallX = strokeRadius
        bgStrokeRectF = RectF(0F, 0F, viewWidth.toFloat(), viewHeight.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isChecked) {
            switchBallX = ballXRight
            bgPaint.color = checkColor
        } else {
            switchBallX = strokeRadius
            bgPaint.color = uncheckColor
        }
        drawBg(canvas)
        drawBall(canvas)
    }

    private fun drawBg(canvas: Canvas) {
        canvas.drawRoundRect(bgStrokeRectF, strokeRadius.toFloat(), strokeRadius.toFloat(), bgPaint)
    }

    private fun drawBall(canvas: Canvas) {
        canvas.drawCircle(switchBallX.toFloat(), strokeRadius.toFloat(), solidRadius.toFloat(), ballPaint)
    }

    private fun setPaint(paint: Paint, paintColor: Int) {
        paint.color = paintColor
        paint.isDither = true
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
    }

    fun setCheck(checked: Boolean) {
        if (checked == isChecked) {
            return
        }
        isChecked = checked
        postInvalidate()
    }

    override fun onClick(v: View) {
        if (isChecked) {
            animate(ballXRight.toFloat(), strokeRadius.toFloat(), checkColor, uncheckColor)
        } else {
            animate(strokeRadius.toFloat(), ballXRight.toFloat(), uncheckColor, checkColor)
        }
        isChecked = !isChecked
    }

    private fun animate(from: Float, to: Float, startColor: Int, endColor: Int) {

        AnimatorSet().apply {
            playTogether(
                ValueAnimator.ofFloat(from, to)
                    .apply {
                        addUpdateListener {
                            switchBallX = (it.animatedValue as? Float?)?.toInt() ?: return@addUpdateListener
                            postInvalidate()
                        }
                    },
                ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
                    .apply {
                        addUpdateListener {
                            bgPaint.color = it.animatedValue as? Int? ?: return@addUpdateListener
                            postInvalidate()
                        }
                    })
            duration = 200
            addListener {
                doOnEnd { this@SwitchButton.isClickable = true }
                doOnStart { this@SwitchButton.isClickable = false }
            }
        }.start()
    }
}