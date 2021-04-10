package com.munch.test.project.one.recyclerview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.pre.lib.helper.drawTextInYCenter
import com.munch.test.project.one.R
import java.util.*

/**
 * Create munch1182 on 2021/4/10 14:32.
 */
class HeaderItemDecoration(
    rv: RecyclerView,
    private val sticky: Boolean = true,
    private val data: (pos: Int) -> IsHeader?,
) :
    RecyclerView.ItemDecoration() {

    @ColorInt
    var stickyBgColor: Int = Color.parseColor("#dadada")

    @ColorInt
    var titleColor: Int = Color.BLACK

    @Px
    var padding = rv.context.resources.getDimension(R.dimen.padding_def)

    @Px
    var headHeight = 120

    private val headHeight2 = headHeight / 2f
    private val padding2 = padding * 3f / 2f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 50f
        color = Color.BLACK
    }
    private var manager: LinearLayoutManager = rv.layoutManager?.takeIf {
        it is LinearLayoutManager && it.orientation == LinearLayoutManager.VERTICAL
    } as? LinearLayoutManager
        ?: throw UnsupportedOperationException("support LinearLayoutManager and LinearLayoutManager.VERTICAL only")
    private var stickyStr = ""

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val pos = parent.getChildAdapterPosition(view).takeIf { it != -1 } ?: return
        val app = data.invoke(pos) ?: return
        outRect.set(0, if (app.isHeaderItem()) headHeight else 1, 0, 0)
    }

    override fun onDrawOver(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDrawOver(c, parent, state)
        parent.children.forEach {
            val pos =
                parent.getChildAdapterPosition(it).takeIf { i -> i != -1 } ?: return
            val app = data.invoke(pos) ?: return
            if (app.isHeaderItem()) {
                paint.color = titleColor
                c.drawTextInYCenter(
                    app.headerStr().toUpperCase(Locale.ROOT),
                    it.x + padding2,
                    it.y - headHeight2, paint
                )
            }
        }
        if (!sticky) {
            return
        }
        val firstPos = manager.findFirstVisibleItemPosition()
        if (firstPos == RecyclerView.NO_POSITION) {
            return
        }
        val header = data.invoke(firstPos) ?: return
        stickyStr = header.headerStr()
        val header2 = data.invoke(firstPos + 1) ?: return
        var y = headHeight.toFloat()
        if (header2.isHeaderItem()) {
            val itemView = manager.findViewByPosition(firstPos + 1) ?: return
            val top = itemView.y - headHeight.toFloat()
            if (top <= headHeight.toFloat()) {
                y = top
            }
        }
        val paddingTop = parent.paddingTop.toFloat()
        paint.color = stickyBgColor
        c.drawRect(0f, paddingTop, parent.width.toFloat(), y, paint)
        paint.color = titleColor
        c.drawTextInYCenter(stickyStr, padding2, y - headHeight2 + paddingTop, paint)
    }

    interface IsHeader {

        fun isHeaderItem(): Boolean

        fun headerStr(): String
    }
}