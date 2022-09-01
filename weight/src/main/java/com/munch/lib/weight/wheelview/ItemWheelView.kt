package com.munch.lib.weight.wheelview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.munch.lib.extend.getLineHeight
import com.munch.lib.extend.measureTextBounds

class ItemWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : BaseWheelView(context, attrs, defStyleAttr, defStyleRes) {

    private val strItem = StringItemListener()
    override val item: OnItemListener = strItem

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        val w = if (wMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(widthMeasureSpec)
        } else {
            paint.measureText(strItem.data.maxBy { it.length }).toInt()
        }
        val h = if (hMode == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            paint.getLineHeight().toInt() * showItemCount
        }
        setMeasuredDimension(w, h)
    }


    fun set(data: Array<String>): ItemWheelView {
        strItem.set(data)
        return this
    }

    fun select(index: Int): ItemWheelView {
        strItem.currIndex = index
        return this
    }

    private class StringItemListener : OnItemListener {

        override var currIndex: Int = 0
        var data = arrayOf<String>()

        override fun onItem(index: Int): String {
            return data[index]
        }

        override fun onIndexValid(index: Int): Boolean {
            return index in data.indices
        }

        fun set(data: Array<String>) {
            this.data = data
        }
    }
}