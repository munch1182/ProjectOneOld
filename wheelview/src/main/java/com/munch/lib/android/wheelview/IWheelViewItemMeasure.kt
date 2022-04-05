package com.munch.lib.android.wheelview

import android.graphics.Rect
import android.text.TextPaint

/**
 * Created by munch1182 on 2022/4/4 7:16.
 */
interface IWheelViewItemMeasure {

    /**
     * 返回item所需的宽高
     *
     * 此方法的默认实现要求所以item的显示是等宽等高的，否则需要自行实现
     *
     * 此方法不是必然被调用，其返回值也不是必然被使用
     *
     * @param rect 可直接使用此参数并且返回，而不要频繁建立对象
     */
    fun onMeasureItem(dataProvider: IWheelViewData<String>, paint: TextPaint, rect: Rect): Rect {
        val curr = dataProvider.curr
        paint.getTextBounds(curr, 0, curr.length, rect)
        //因为使用StaticLayout时需要的宽度更宽
        rect.right += rect.height() / 2
        return rect
    }
}

class DefaultWheelViewItemMeasurer : IWheelViewItemMeasure