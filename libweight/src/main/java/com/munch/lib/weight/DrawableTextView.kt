package com.munch.lib.weight

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.munch.lib.android.log.log

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
    }

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
        }.recycle()
        // 因此需要再次手动调用一次设置一次宽高
        val drawables = compoundDrawables // 不包含drawStart和drawEnd, 因此不能使用这两个设置
        setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])
    }

    /**
     *  解析drawLeft等相关方法会调用[setCompoundDrawablesWithIntrinsicBounds], 然后调用此方法, 所以在此处覆盖宽高设置
     */
    override fun setCompoundDrawables(
        left: Drawable?,
        top: Drawable?,
        right: Drawable?,
        bottom: Drawable?
    ) {
        if (whs != null) {
            setWHByWHS(left, 0)
            setWHByWHS(top, 1)
            setWHByWHS(right, 2)
            setWHByWHS(bottom, 3)
        }
        super.setCompoundDrawables(left, top, right, bottom)
    }

    private fun setWHByWHS(drawable: Drawable?, index: Int) {
        val whs = this.whs ?: return
        try {
            drawable?.let {
                val w = if (whs[index * 2] == UNSET_WH) it.intrinsicWidth else whs[index * 2]
                val h =
                    if (whs[index * 2 + 1] == UNSET_WH) it.intrinsicWidth else whs[index * 2 + 1]
                it.setBounds(0, 0, w, h)
            }
        } catch (e: Exception) {
            log(e)
        }
    }
}