package com.munch.lib.extend.recyclerview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.log

/**
 * 通过移动过长的距离然后观测到第一个可见item是目标item时立即停止来实现从下往上的平滑移动
 *
 * 然而这种方式并不稳定，可能会有一定像素的差异，可能是onScrolled处理速度较慢，也可能是调用方法不对
 *
 * 还是得自行实现[androidx.recyclerview.widget.RecyclerView.LayoutManager.smoothScrollToPosition]
 *
 * Create by munch1182 on 2021/1/17 23:32.
 */
class FirstScrollHelper {

    private var rv: RecyclerView? = null
    private val unset = -1
    private var lm: LinearLayoutManager? = null
    private var height = lm?.height ?: unset
    private var target = unset
    private var isAdd = false
    private var needScrollHandle = false
    private val firstScrollListener by lazy {
        object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (unset() || !needScrollHandle) {
                    return
                }
                val firstPos = lm!!.findFirstVisibleItemPosition()
                log(lm?.getChildAt(target)?.y)
                if (firstPos == target) {
                    //停止滑动
                    recyclerView.stopScroll()
                    clear()
                }
            }
        }
    }

    fun bind(recyclerView: RecyclerView) {
        this.rv = recyclerView
        if (!isAdd) {
            rv!!.addOnScrollListener(firstScrollListener)
            isAdd = true
        }
        collectSet(unset)
    }

    private fun clear() {
        target = unset
    }

    private fun collectSet(pos: Int) {
        target = pos
        rv ?: return
        if (lm == null) {
            lm = (rv!!.layoutManager as? LinearLayoutManager?)?.takeIf {
                it.orientation == LinearLayoutManager.VERTICAL
            }
        }
        if (height == unset) {
            height = rv!!.getChildAt(0)?.height ?: unset
        }
    }

    private fun unset(): Boolean {
        return target == unset || height == unset || lm == null || rv == null || !isAdd
    }

    /**
     * 平滑移动到某个位置并且指定到顶部
     * 不适合item高度变化差异大的
     *
     * 水平方向未实现
     */
    fun smoothScrollToPosThenStopFirst(pos: Int) {
        collectSet(pos)
        if (unset()) {
            return
        }
        val firstPos = lm!!.findFirstVisibleItemPosition()
        val lastPos = lm!!.findLastVisibleItemPosition()
        if (firstPos == -1 || lastPos == -1) {
            return
        }
        val pageItem = lastPos - firstPos

        //位置在列表前
        when {
            pos < firstPos -> {
                //自动实现
                needScrollHandle = false
                rv!!.smoothScrollToPosition(pos)
                //位置在列表后
            }
            pos > lastPos -> {
                //移动超过该有的距离，然后当第一个可见view为指定点的view时停止
                //但不能过长，因为有动画差异
                needScrollHandle = true
                val i = (pos - lastPos) + pageItem + pageItem / 2
                rv!!.smoothScrollBy(0, i * height)
                //位置在屏幕上
            }
            else -> {
                needScrollHandle = false
                val posView = lm!!.getChildAt(pos) ?: return
                val firView = lm!!.getChildAt(firstPos) ?: return
                rv!!.smoothScrollBy(0, (posView.y - firView.y).toInt())
            }
        }
    }

}