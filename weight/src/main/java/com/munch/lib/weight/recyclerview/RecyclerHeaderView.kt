package com.munch.lib.weight.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.weight.calendar.MonthDrawer
import kotlin.math.max
import kotlin.math.min

/**
 * Create by munch1182 on 2022/6/6 17:03.
 */
open class RecyclerHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    protected open val child: View by lazy { getChildAt(0) }
    protected open val recyclerView: RecyclerView by lazy { getChildAt(1) as RecyclerView }
    private val monthHeight = MonthDrawer(context).onItemMeasure().h
    private val lm by lazy {
        if (recyclerView.layoutManager == null) {
            LinearLayoutManager(context).apply { recyclerView.layoutManager = this }
        } else {
            recyclerView.layoutManager as LinearLayoutManager
        }
    }
    private val scrollListener by lazy {
        object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val first = lm.findFirstVisibleItemPosition()
                //判断更新通知
                if (lastItemPos != first) {
                    lastItemPos = first
                    updateFirstPos(lastItemPos)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val first = lm.findFirstVisibleItemPosition()
                    val last = lm.findLastCompletelyVisibleItemPosition()
                    if (last == ((recyclerView.adapter?.itemCount ?: 0) - 1)) {
                        return
                    }
                    //第二个view
                    val sec = lm.findViewByPosition(first + 1) ?: return

                    if (sec.top == monthHeight) {
                        return
                    }

                    if (sec.top > sec.height / 2) {
                        scroller.targetPosition = first
                    } else {
                        scroller.targetPosition = first + 1
                    }
                    lm.startSmoothScroll(scroller)
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        recyclerView.addOnScrollListener(scrollListener)
    }

    private val scroller by lazy {
        object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                //左边值越大越慢
                return 75f / displayMetrics!!.densityDpi
            }
        }
    }
    private var lastItemPos = -1

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
        recyclerView.layout(l, t + child.measuredHeight - monthHeight, r, b)
        child.layout(l, t, r, t + child.measuredHeight)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 2) {
            throw IllegalStateException()
        }
    }

    /**
     * 当一个item变更时(不管高度)，会调用此方法
     */
    protected open fun updateFirstPos(itemPos: Int) {
    }

}