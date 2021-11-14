package com.munch.lib.helper

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2021/11/14 3:40.
 */
class RecyclerViewHelper(private val rv: RecyclerView) {

    private var lastPos = 0
    private var lastOffset = 0
    private val lm: LinearLayoutManager
        get() = rv.layoutManager as? LinearLayoutManager ?: throw UnsupportedOperationException()

    fun updateMove() {
        lastPos = lm.findFirstVisibleItemPosition()
        lastOffset = lm.getChildAt(lastPos)?.top ?: 0
    }

    fun restore() {
        if (lastPos == 0 && lastOffset == 0) {
            return
        }
        lm.scrollToPositionWithOffset(lastPos, lastOffset)
    }

    fun listenScroll() {
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateMove()
                }
            }
        })
    }
}