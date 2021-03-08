package com.munch.lib.test.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.BaseApp
import com.munch.lib.helper.drawTextInYCenter
import com.munch.lib.test.R

/**
 * Create by munch1182 on 2021/1/17 21:31.
 */
class HeaderItemDecoration(
    private var sticky: Boolean = true,
    private var data: ArrayList<IsHeader> = ArrayList(0)
) :
    RecyclerView.ItemDecoration() {

    private val headHeight = 80
    private val dividerHeight = 1
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 50f
        color = Color.BLACK
    }
    private val bgColor = Color.parseColor("#dadada")
    private val padding = BaseApp.getContext().resources.getDimension(R.dimen.padding_def)
    private var judgeLayoutManager = 0
    private var stickyStr = ""
    private var layoutManager: LinearLayoutManager? = null

    fun resetData(list: ArrayList<IsHeader> = ArrayList(0)) {
        data = list
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        //只处理垂直方向的LinearLayoutManager
        if (judgeLayoutManager(parent)) return

        //直接更改背景颜色，用以显示分割线
        parent.setBackgroundColor(bgColor)
        var child: View?
        paint.color = Color.BLACK
        for (i in 0 until parent.childCount) {
            child = parent.getChildAt(i)
            val pos = parent.getChildAdapterPosition(child)
            val item = data[pos]
            if (item.isHeaderItem()) {
                c.drawTextInYCenter(
                    item.headerStr(),
                    child.x + 3f / 2f * padding,
                    child.y - headHeight / 2f,
                    paint
                )
            }
        }
        if (!sticky) {
            return
        }
        layoutManager ?: return
        val firstPos = layoutManager!!.findFirstVisibleItemPosition()
        if (firstPos == -1) {
            return
        }
        val isHeader = data[firstPos]
        stickyStr = isHeader.headerStr()

        val isHeader2 = data[firstPos + 1]
        var y: Float = headHeight.toFloat()
        if (isHeader2.isHeaderItem()) {
            val itemView = layoutManager!!.findViewByPosition(firstPos + 1) ?: return
            //rect的高不算在item的y里面
            val top = itemView.y - headHeight.toFloat()
            if (top <= headHeight.toFloat()) {
                y = top
            }
        }
        paint.color = bgColor
        val paddingTop = parent.paddingTop.toFloat()
        c.drawRect(0f, paddingTop, parent.width.toFloat(), y, paint)
        paint.color = Color.BLACK
        c.drawTextInYCenter(stickyStr, 3f / 2f * padding, y - headHeight / 2f + paddingTop, paint)
    }

    /**
     * 只处理垂直方向的LinearLayoutManager
     */
    private fun judgeLayoutManager(parent: RecyclerView): Boolean {
        if (judgeLayoutManager != 0) {
            return judgeLayoutManager == 2
        }
        layoutManager =
            parent.layoutManager?.takeIf { it is LinearLayoutManager && it.orientation == LinearLayoutManager.VERTICAL } as? LinearLayoutManager?
        judgeLayoutManager = if (layoutManager == null) 2 else 1
        return judgeLayoutManager == 2
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (judgeLayoutManager(parent)) return
        for (i in 0 until parent.childCount) {
            val pos = parent.getChildAdapterPosition(view)
            if (pos == -1) {
                return
            }
            if (data[pos].isHeaderItem()) {
                outRect.set(0, headHeight, 0, 0)
            } else {
                //第一个无需分割线
                //露出一个像素的高度，露出背景的颜色，或者自己绘制
                outRect.set(0, if (pos == 0) 0 else dividerHeight, 0, 0)
            }
        }
    }

    interface IsHeader {

        fun isHeaderItem(): Boolean

        fun headerStr(): String
    }
}