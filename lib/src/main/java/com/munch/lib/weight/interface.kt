package com.munch.lib.weight

import android.content.res.TypedArray
import android.graphics.Canvas
import android.view.View

/**
 * Create by munch1182 on 2021/8/14 15:24.
 */
interface ViewImp<V : View> : ViewUpdate<LoadingView> {

    val view: V

    fun obtainStyledAttributes(typedArray: TypedArray) {}

    fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {}

    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {}

    fun onLayout(onChange: Boolean, l: Int, t: Int, b: Int, r: Int) {}

    fun onDraw(canvas: Canvas) {}
}

interface ViewUpdate<T : View> {

    /**
     * 用于一次性更改多个属性，然后统一刷新
     * 具体刷新方法自定义view应自行调用[View.requestLayout]或者[View.invalidate]
     */
    @Suppress("UNCHECKED_CAST")
    fun set(set: T.() -> Unit) {
        set.invoke(this as T)
    }
}