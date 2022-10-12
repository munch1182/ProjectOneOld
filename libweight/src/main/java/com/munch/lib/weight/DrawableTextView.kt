package com.munch.lib.weight

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * Create by munch1182 on 2022/10/8 17:11.
 */
class DrawableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0,
) : AppCompatTextView(context, attrs, styleDef) {

    companion object {
        private const val UNSET_WH = -1
        private val START = Direction(0)
        private val TOP = Direction(1)
        private val END = Direction(2)
        private val BOTTOM = Direction(3)
    }

    @JvmInline
    private value class Direction(val level: Int)

    /**
     * 根据left/top/right/bottom依次存储对于drawable的宽高
     *
     *  *** 并非构造函数中的属性, 所以不能绕过父构造先初始化这个属性 ***
     *  当[android.widget.TextView]在构造中解析drawable并调用[setCompoundDrawables]时, 此属性仍未初始化, 所以需要设为可为null
     */
    private var whs: IntArray? = null

    init {
        val whs = IntArray(8) { UNSET_WH }
        this.whs = whs
        context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView).apply {
            val with =
                getDimensionPixelOffset(R.styleable.DrawableTextView_dtv_drawableWidth, UNSET_WH)
            val height =
                getDimensionPixelOffset(R.styleable.DrawableTextView_dtv_drawableHeight, UNSET_WH)
            if (with != UNSET_WH || height != UNSET_WH) {
                whs.forEachIndexed { index, _ ->
                    if (index % 2 == 0 && with != UNSET_WH) {
                        whs[index] = with
                    } else if (index % 2 == 1 && height != UNSET_WH) {
                        whs[index] = height
                    }
                }
            }
            setWHS(
                this, START,
                R.styleable.DrawableTextView_dtv_drawableStartWidth,
                R.styleable.DrawableTextView_dtv_drawableStartHeight
            )
            setWHS(
                this, TOP,
                R.styleable.DrawableTextView_dtv_drawableTopWidth,
                R.styleable.DrawableTextView_dtv_drawableTopHeight
            )
            setWHS(
                this, END,
                R.styleable.DrawableTextView_dtv_drawableEndWidth,
                R.styleable.DrawableTextView_dtv_drawableEndHeight
            )
            setWHS(
                this, BOTTOM,
                R.styleable.DrawableTextView_dtv_drawableBottomWidth,
                R.styleable.DrawableTextView_dtv_drawableBottomHeight
            )
        }.recycle()
        // 因此需要再次手动调用一次设置一次宽高
        val drawables = compoundDrawablesRelative // 包含drawStart和drawEnd, 不支持drawLeft和drawRight
        setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])
    }

    private fun setWHS(type: TypedArray, direction: Direction, widthId: Int, heightId: Int) {
        val with = type.getDimensionPixelOffset(widthId, UNSET_WH)
        val height = type.getDimensionPixelOffset(heightId, UNSET_WH)
        if (with != UNSET_WH) {
            whs?.set(direction.level * 2, with)
        }
        if (height != UNSET_WH) {
            whs?.set(direction.level * 2 + 1, height)
        }
    }

    override fun setCompoundDrawables(
        left: Drawable?,
        top: Drawable?,
        right: Drawable?,
        bottom: Drawable?
    ) {
        if (whs != null) {
            setWHByWHS(left, START)
            setWHByWHS(top, TOP)
            setWHByWHS(right, END)
            setWHByWHS(bottom, BOTTOM)
        }
        super.setCompoundDrawables(left, top, right, bottom)
    }

    private fun setWHByWHS(drawable: Drawable?, direction: Direction) {
        val whs = this.whs ?: return
        drawable?.let {
            val index4W = direction.level * 2
            val index4H = index4W + 1
            val w = if (whs[index4W] == UNSET_WH) it.intrinsicWidth else whs[index4W]
            val h = if (whs[index4H] == UNSET_WH) it.intrinsicHeight else whs[index4H]
            it.setBounds(0, 0, w, h)
        }
    }
}