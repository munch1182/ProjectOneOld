package com.munch.lib.fast.weight

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Checkable
import com.munch.lib.fast.R
import com.munch.pre.lib.extend.dp2Px

/**
 * Create by munch1182 on 2021/6/22 9:54.
 */
@SuppressLint("UseSwitchCompatOrMaterialCode")
class SwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(
    context, attrs, defStyleAttr, defStyleRes
), Checkable {

    companion object {
        private const val DEF_WIDTH = 40f
        private const val DEF_HEIGHT = 24f
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rectTemp = RectF()
    private val helper: PointHelper = PointHelper()
    private var paddingCenter = 8f
    private var bgColor = Color.parseColor("#29272C31")
    private var checkBgColor = Color.parseColor("#DAB177")
    private var mainColor = Color.WHITE
    private var checkedMainColor = Color.WHITE
    private var current = 0f
    private var checked = false
    private var needAnim = false
    private var onChecked: ((isChecked: Boolean) -> Unit)? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        context.obtainStyledAttributes(attrs, R.styleable.SwitchView).apply {
            bgColor = getColor(R.styleable.SwitchView_sv_bg_color, bgColor)
            checkBgColor = getColor(R.styleable.SwitchView_sv_checked_bg_color, checkBgColor)
            mainColor = getColor(R.styleable.SwitchView_sv_main_color, mainColor)
            checkedMainColor =
                getColor(R.styleable.SwitchView_sv_checked_main_color, checkedMainColor)
            checked = getBoolean(R.styleable.SwitchView_sv_checked, false)
            setChecked(checked, false)
        }.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val width = if (widthMode != MeasureSpec.EXACTLY) context.dp2Px(DEF_WIDTH)
        else MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val height = if (widthMode != MeasureSpec.EXACTLY) {
            if (widthMode != MeasureSpec.EXACTLY) {
                context.dp2Px(DEF_HEIGHT).toInt()
            } else {
                width * 0.55f
            }
        } else {
            MeasureSpec.getSize(widthMeasureSpec)
        }
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        helper.apply {
            bg.set(0f, 0f, w.toFloat(), h.toFloat())
            center.set(w / 2f, h / 2f)
            radius = h / 2f - paddingCenter
            val centerDis = radius + paddingCenter
            left.set(bg.left + centerDis, bg.top + centerDis)
            right.set(bg.right - centerDis, bg.bottom - centerDis)
            translateX = right.x - left.x
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        drawBg(canvas)
        if (needAnim) {
            canvas.save()
            translate(canvas)
            drawCenter(canvas)
            canvas.restore()
        } else {
            drawCenterCheck(canvas)
        }
    }

    private fun drawCenterCheck(canvas: Canvas) {
        paint.color = if (checked) checkedMainColor else mainColor
        val x = if (isChecked) helper.right.x else helper.left.x
        val y = if (isChecked) helper.right.y else helper.left.y
        canvas.drawCircle(x, y, helper.radius, paint)
        onChecked?.invoke(isChecked)
    }

    private fun translate(canvas: Canvas) {
        current += 0.1f
        canvas.translate(helper.translateX * (if (isChecked) current else 1 - current), 0f)
        if (0f < current && current < 1f) {
            invalidate()
        } else {
            needAnim = false
            current = 0f
            onChecked?.invoke(isChecked)
        }
    }

    private fun drawCenter(canvas: Canvas) {
        paint.color = if (checked) checkedMainColor else mainColor
        canvas.drawCircle(helper.left.x, helper.left.y, helper.radius, paint)
    }

    private fun drawBg(canvas: Canvas) {
        rectTemp.set(helper.bg)
        rectTemp.left = 0f
        rectTemp.right = helper.radius * 2 + paddingCenter * 2
        path.arcTo(rectTemp, 90f, 180f)
        rectTemp.left = helper.right.x - helper.radius - paddingCenter
        rectTemp.right = helper.bg.right
        path.arcTo(rectTemp, 270f, 180f)
        path.close()

        paint.color = if (isChecked) checkBgColor else bgColor
        canvas.drawPath(path, paint)
    }

    private data class PointHelper(
        var bg: RectF = RectF(),
        var center: PointF = PointF(),
        var radius: Float = 0f,
        var left: PointF = PointF(),
        var right: PointF = PointF(),
        var translateX: Float = 0f
    )

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            //按下之后需等到抬起才能进行下次更新
            performClick()
            needAnim = true
            setChecked(!isChecked, true)
        }
        return true
    }

    private fun setChecked(checked: Boolean, anim: Boolean) {
        if (this.checked != checked) {
            this.checked = checked
            needAnim = anim
            invalidate()
        }
    }

    override fun setChecked(checked: Boolean) {
        setChecked(checked, true)
    }

    override fun isChecked() = checked

    override fun toggle() {
        isChecked = !isChecked
    }

    fun setOnCheckedChangeListener(onChecked: ((isChecked: Boolean) -> Unit)?) {
        this.onChecked = onChecked
    }
}