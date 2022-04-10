package com.munch.lib.android.wheelview

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.munch.lib.extend.setDashPath

/**
 * Created by munch1182 on 2022/4/3 22:12.
 */
class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleDef: Int = 0,
    private val drawImp: IWheelViewDraw = DefaultWheelViewDrawer(),
    private val builder: IWheelViewConfig = WheelViewBuilder().build(),
    private val dataProvider: IWheelViewData<String> = DefaultWheelViewDataProvider(),
    private val itemMeasurer: IWheelViewItemMeasure = DefaultWheelViewItemMeasurer()
) : View(context, attrs, styleDef), IWheelViewConfig by builder {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val tempRect = Rect()

    //一个item的范围
    private val itemRect = Rect()

    //中心点
    private val centerPoint = PointF()

    /**
     * 选中的item到临近的item的y轴中心点的距离
     */
    private var itemCenterYDistance = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        var itemWidth = builder.itemWidth
        var itemHeight = builder.itemHeight

        textPaint.textSize = builder.selectedTextSize
        if (itemWidth == -1 || itemHeight == -1) {
            val rect = itemMeasurer.onMeasureItem(dataProvider, textPaint, tempRect)
            if (itemWidth == -1) {
                itemWidth = rect.width()
            }
            if (itemHeight == -1) {
                itemHeight = rect.height()
            }
        }

        itemRect.set(0, 0, itemWidth, itemHeight)
        itemCenterYDistance = itemHeight + builder.itemDistance

        val w = paddingLeft + paddingRight + //内padding
                itemWidth +  //最大的item的宽度
                builder.itemPaddingLeft + builder.itemPaddingRight //item的内置padding

        val h = paddingTop + paddingBottom + //内padding
                itemHeight + //中心选中item的高度
                (builder.itemDistance + itemHeight) * 2 * builder.itemOffsetNumber +  //未选中item的高度
                0//itemHeight / 2 //补充高度

        setMeasuredDimension(w, h)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        //绘制当前的选中的item
        textPaint.textSize = builder.selectedTextSize
        textPaint.color = builder.selectedTextColor

        val curr = dataProvider.curr

        val start = paddingLeft.toFloat()
        val end = (width - paddingRight).toFloat()
        val centerY = height / 2f
        val driverDistance = itemRect.height() / 2f + builder.itemDistance / 2f

        centerPoint.set(width / 2f, height / 2f)
        drawImp.onDrawText(curr, canvas, textPaint, itemRect.width(), centerPoint)

        paint.setDashPath(Color.WHITE)
        canvas.drawLine(0f, centerPoint.y, width.toFloat()/2F, centerPoint.y, paint)
        /*canvas.drawRect(
            0f,
            centerPoint.y - itemRect.height() / 2f,
            width.toFloat(),
            centerPoint.y + itemRect.height() / 2f,
            paint
        )*/

        repeat(builder.itemOffsetNumber) {
            val offset = it + 1
            var value = dataProvider.onValueOffset(curr, -offset)
            centerPoint.y = centerY - itemCenterYDistance * offset
            drawImp.onDrawText(value, canvas, textPaint, itemRect.width(), centerPoint)

            paint.setDashPath(Color.WHITE)
            canvas.drawLine(0f, centerPoint.y, width.toFloat()/2F, centerPoint.y, paint)

            paint.color = builder.driverLineColor
            paint.strokeWidth = builder.driverLineHeight

            centerPoint.y += driverDistance
            drawImp.onDrawLine(
                canvas,
                paint,
                floatArrayOf(start, centerPoint.y, end, centerPoint.y)
            )

            centerPoint.y = centerY + itemCenterYDistance * offset
            value = dataProvider.onValueOffset(curr, offset)
            drawImp.onDrawText(value, canvas, textPaint, itemRect.width(), centerPoint)

            paint.setDashPath(Color.WHITE)
            canvas.drawLine(0f, centerPoint.y, width.toFloat()/2F, centerPoint.y, paint)

            paint.color = builder.driverLineColor
            paint.strokeWidth = builder.driverLineHeight
            centerPoint.y -= driverDistance
            drawImp.onDrawLine(
                canvas,
                paint,
                floatArrayOf(start, centerPoint.y, end, centerPoint.y)
            )
        }

    }

}