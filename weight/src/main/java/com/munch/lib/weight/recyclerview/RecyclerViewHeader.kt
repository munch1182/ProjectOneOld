package com.munch.lib.weight.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.log.log
import kotlin.math.max
import kotlin.math.min

/**
 * Create by munch1182 on 2022/6/6 17:03.
 */
class RecyclerViewHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val child by lazy { getChildAt(0) }
    private val recyclerView: RecyclerView by lazy { getChildAt(1) as RecyclerView }

    private var lastItemPos = -1

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 2) {
            throw IllegalStateException()
        }
        if (childCount == 2 && getChildAt(1) !is RecyclerView) {
            throw IllegalStateException()
        }
        val lm = if (recyclerView.layoutManager == null) {
            LinearLayoutManager(context).apply { recyclerView.layoutManager = this }
        } else {
            recyclerView.layoutManager as LinearLayoutManager
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val first = lm.findFirstVisibleItemPosition()
                    //判断更新通知
                    if (lastItemPos != first) {
                        lastItemPos = first
                        updateFirstPos(lastItemPos)
                    }
                    //第二个view
                    val sec = lm.findViewByPosition(first + 1) ?: return

                    if (sec.top > sec.height / 2) {
                        recyclerView.scrollBy(0, -(sec.height - sec.top))
                        log(1,sec.height - sec.top)
                    } else {
                        recyclerView.scrollBy(0, sec.top)
                        log(2,sec.top)
                    }
                }
            }
        })
    }

    /**
     * 当一个item变更时(不管高度)，会调用此方法
     */
    private fun updateFirstPos(itemPos: Int) {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount != 2) {
            return
        }
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        measureChild(child, widthMeasureSpec, heightMeasureSpec)
        measureChild(recyclerView, widthMeasureSpec, heightMeasureSpec)
        val width = min(w, max(recyclerView.measuredWidth, child.measuredWidth))
        val height = min(h, recyclerView.measuredHeight + child.measuredHeight)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        child.layout(l, t, r, t + child.measuredHeight)
        recyclerView.layout(l, t + child.measuredHeight, r, b)
    }
}