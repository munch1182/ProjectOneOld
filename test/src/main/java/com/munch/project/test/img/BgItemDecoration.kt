package com.munch.project.test.img

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.helper.PosHelper
import com.munch.lib.helper.obWhenDestroy
import com.munch.lib.helper.scaleBitmap
import kotlin.math.max

/**
 * Create by munch1182 on 2021/1/15 17:10.
 */
class BgItemDecoration(
    owner: LifecycleOwner,
    private var originBitmap: Bitmap? = null
) : RecyclerView.ItemDecoration() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var scaleBitmap: Bitmap? = null
    private val viewRect = Rect()
    private val bitmapRect = Rect()
    private var parallax = 1f
    private var topOffset = 0
    private var leftOffset = 0

    init {
        obWhenDestroy(owner) {
            clear()
        }
    }

    fun setBg(bitmap: Bitmap): BgItemDecoration {
        this.originBitmap = bitmap
        clearScaleBitmap()
        return this
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (scaleBitmap == null) {
            val childCount = parent.layoutManager?.childCount ?: 1

            changeBitmap(parent)
            scaleBitmap ?: return
            viewRect.set(0, 0, parent.width, parent.height)

            val bitmapWidth = scaleBitmap!!.width.toFloat()
            val maxViewWidth = childCount * viewRect.width().toFloat()
            //因为长度不一导致的视差
            //图片超出最大长度暂不显示
            parallax = bitmapWidth / maxViewWidth

            //取高度差的一半，让高度绘制中心值
            topOffset = (PosHelper.getDis(
                viewRect.height().toFloat(),
                scaleBitmap!!.height.toFloat()
            ) / 2f).toInt()
        }
        scaleBitmap ?: return
        //当前水平偏移量，0 ~ maxViewWidth
        val offset = parent.layoutManager?.computeHorizontalScrollOffset(state)?.toFloat() ?: 0f
        leftOffset = (parallax * offset).toInt()

        bitmapRect.set(
            leftOffset,
            topOffset,
            leftOffset + viewRect.width(),
            topOffset + viewRect.height()
        )
        c.drawBitmap(scaleBitmap!!, bitmapRect, viewRect, paint)
    }

    /**
     * 放大到铺满屏幕
     * 未处理图片过大需要缩小的问题
     */
    private fun changeBitmap(parent: RecyclerView) {
        originBitmap ?: return
        scaleBitmap = originBitmap!!.scaleBitmap(getRatio(originBitmap!!, parent))
        clearOriginBitmap()
    }

    private fun clearOriginBitmap() {
        originBitmap?.recycle()
        originBitmap = null
    }

    private fun getRatio(bitmap: Bitmap, parent: RecyclerView): Float {
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        val vw = parent.width.toFloat()
        val vh = parent.height.toFloat()
        if (bw < vw || bh < vh) {
            return max(vw / bw, vh / bh)
        }
        return 1f
    }

    fun clear() {
        clearOriginBitmap()
        clearScaleBitmap()
    }

    private fun clearScaleBitmap() {
        this.scaleBitmap?.recycle()
        this.scaleBitmap = null
    }
}