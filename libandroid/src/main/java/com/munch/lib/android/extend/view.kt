package com.munch.lib.android.extend

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.munch.lib.android.define.ViewProvider

/**
 * 获取一个新的(WRAP_CONTENT,WRAP_CONTENT)的LayoutParams
 */
val newWWLP: ViewGroup.LayoutParams
    get() = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

/**
 * 获取一个新的(MATCH_PARENT,WRAP_CONTENT)的LayoutParams
 */
val newMWLP: ViewGroup.LayoutParams
    get() = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

val View.paddingHorizontal: Int
    get() = paddingLeft + paddingRight

val View.paddingVertical: Int
    get() = paddingTop + paddingBottom

/**
 * 设置View的点击效果, 会替换背景和顶部的Drawable
 */
fun View.clickEffect(color: Int = Color.WHITE) {
    background = ColorDrawable(color)
    foreground = getSelectableItemBackground()
}

//<editor-fold desc="gravity">
/**
 * 当前int值是否包含[gravity], 注意:并不是所有的[Gravity]都支持
 */
fun Int.hasGravity(gravity: Int): Boolean {
    return when (gravity) {
        Gravity.START, Gravity.END, Gravity.CENTER_HORIZONTAL -> this and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK == gravity
        Gravity.TOP, Gravity.BOTTOM, Gravity.CENTER_VERTICAL -> this and Gravity.VERTICAL_GRAVITY_MASK == gravity
        else -> false
    }
}

/**
 * 返回当前int值所包含的水平方向的[Gravity], 否则会返回Gravity.NO_GRAVITY
 */
fun Int.getHorizontalGravity(): Int {
    return when {
        hasGravity(Gravity.START) -> Gravity.START
        hasGravity(Gravity.END) -> Gravity.END
        hasGravity(Gravity.CENTER_HORIZONTAL) -> Gravity.CENTER_HORIZONTAL
        else -> Gravity.NO_GRAVITY
    }
}

/**
 * 返回当前int值所包含的垂直方向的[Gravity], 否则会返回Gravity.NO_GRAVITY
 */
fun Int.getVerticalGravity(): Int {
    return when {
        hasGravity(Gravity.TOP) -> Gravity.TOP
        hasGravity(Gravity.BOTTOM) -> Gravity.BOTTOM
        hasGravity(Gravity.CENTER_VERTICAL) -> Gravity.CENTER_VERTICAL
        else -> Gravity.NO_GRAVITY
    }
}
//</editor-fold>

/**
 * 双击回调
 * 两次点击之间间隔必须小于[doubleTime]
 *
 * 因为占用了点击事件, 所以不能再设计点击事件, 否则会被覆盖
 */
fun View.setDoubleClickListener(doubleTime: Long = 500L, l: View.OnClickListener?) {
    setOnClickListener(object : View.OnClickListener {
        private var lastClick = 0L
        override fun onClick(v: View?) {
            val now = System.currentTimeMillis()
            if (now - lastClick < doubleTime) {
                l?.onClick(v)
            }
            lastClick = now
        }
    })
}

fun View.addPadding(p: Int = 0, l: Int = p, t: Int = p, r: Int = p, b: Int = p) {
    setPadding(paddingStart + l, paddingTop + t, paddingEnd + r, paddingBottom + b)
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.addPadding(padding: Int) = addPadding(p = padding)

/**
 * 一个使用颜色线分割的RecyclerView.ItemDecoration
 */
class LinearLineItemDecoration(
    private val lineHeight: Float = 1.5f,
    lineColor: Int = Color.parseColor("#f4f4f4")
) : RecyclerView.ItemDecoration() {

    private var lm: LinearLayoutManager? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineHeight
        color = lineColor
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (lm == null) {
            lm = parent.layoutManager?.to()
        }
        lm?.apply {
            val fp = findFirstVisibleItemPosition()
                .takeIf { it != RecyclerView.NO_POSITION }
                ?: return
            val ep = findLastVisibleItemPosition()
                .takeIf { it != RecyclerView.NO_POSITION }
                ?: return

            val l = parent.left.toFloat()
            val r = parent.right.toFloat()
            var y: Float
            for (i in fp..ep) {
                findViewByPosition(i)?.let {
                    y = it.top.toFloat()
                    c.drawLine(l, y, r, y, paint)
                }
            }
            if (fp == 0) {
                y = parent.top.toFloat() + lineHeight
                c.drawLine(l, y, r, y, paint)
            }
            if (ep == itemCount - 1) {
                y = parent.bottom.toFloat() - lineHeight
                c.drawLine(l, y, r, y, paint)
            }
        }
    }
}

/**
 * 给[target]提供一个VB
 * 构建的VB可使用[set]来使用并通过[vb]来获取, 而VB构建的View会通过[ViewProvider.setView]传递给[target]
 * 通过[set]方法, 调用对象又会返回[target]
 *
 * 即可以通过给对象[target]创建一个方法返回一个[ViewBindViewHelper]的实现对象, 并将[VB]设置给[TARGET], 然后再链式返回到[TARGET]
 *
 * class A {
 *
 *  fun <VB:ViewBinding> bind() = object : ViewBindViewHelper<VB, A>(this, context) {}
 *
 *  fun other(){}
 *
 *  override fun setView(view: View) {}
 *
 * }
 *
 * // 使用:
 * val a = A().bind<VB>.set{}.other()
 */
abstract class ViewBindViewHelper<VB : ViewBinding, TARGET : ViewProvider>(
    private val target: TARGET,
    context: Context,
    group: ViewGroup? = null,
    attach: Boolean = false
) {

    val vb: VB by lazy {
        this.javaClass.findParameterized(ViewBinding::class.java)
            ?.inflate(LayoutInflater.from(context), group, attach)!!.to()
    }

    fun create(): TARGET {
        target.setView(vb.root)
        return target
    }

    fun set(set: (VB.() -> Unit)?): TARGET {
        create()
        set?.invoke(vb)
        return target
    }
}