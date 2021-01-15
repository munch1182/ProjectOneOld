package com.munch.project.test.img

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.helper.DrawHelper
import com.munch.lib.helper.obWhenDestroy
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

    init {
        obWhenDestroy(owner) {
            clear()
        }
    }

    fun setBg(bitmap: Bitmap): BgItemDecoration {
        this.originBitmap = bitmap
        this.scaleBitmap?.recycle()
        this.scaleBitmap = null
        return this
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (scaleBitmap == null) {
            changeBitmap(parent)
        }
        scaleBitmap ?: return
        c.drawBitmap(scaleBitmap!!, 0f, 0f, paint)
    }

    private fun changeBitmap(parent: RecyclerView) {
        originBitmap ?: return
        scaleBitmap = DrawHelper.scaleBitmap(originBitmap!!, getRatio(originBitmap!!, parent))
        originBitmap?.recycle()
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
        this.originBitmap?.recycle()
        this.scaleBitmap?.recycle()
        this.originBitmap = null
        this.scaleBitmap = null
    }
}