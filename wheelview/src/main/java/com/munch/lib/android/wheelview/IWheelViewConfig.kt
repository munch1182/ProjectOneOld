package com.munch.lib.android.wheelview

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Created by munch1182 on 2022/4/3 22:15.
 */
interface IWheelViewConfig {

    val driverLineHeight: Float
    val driverLineColor: Int

    val itemOffsetNumber: Int

    val selectedTextColor: Int
    val selectedTextSize: Float

    val unselectedTextColor: Int
    val unselectedTextSize: Float


    val itemPaddingLeft: Int
    val itemPaddingRight: Int

    /**
     * 指的是上下两个item之间的总间距
     */
    val itemDistance: Int

    /**
     * item的宽度
     */
    val itemWidth: Int

    /**
     * item的高度
     */
    val itemHeight: Int
}

class WheelViewBuilder {
    private val config = object : IWheelViewConfig {
        override var driverLineHeight: Float = 2f
        override var driverLineColor: Int = Color.parseColor("#4f4f4f")
        override var itemOffsetNumber: Int = 2
        override var selectedTextColor: Int = Color.BLACK
        override var selectedTextSize: Float = 56f
        override var unselectedTextColor: Int = Color.BLACK
        override var unselectedTextSize: Float = 56f
        override var itemPaddingLeft: Int = 32
        override var itemPaddingRight: Int = 32
        override var itemDistance: Int = 48
        override var itemWidth: Int = -1
        override var itemHeight: Int = -1
    }

    fun setItemWidth(itemWidth: Int): WheelViewBuilder {
        config.itemWidth = itemWidth
        return this
    }

    fun setItemHeight(itemHeight: Int): WheelViewBuilder {
        config.itemHeight = itemHeight
        return this
    }

    fun setDriverLineHeight(driverLineHeight: Float): WheelViewBuilder {
        config.driverLineHeight = driverLineHeight
        return this
    }

    fun setDriverLineColor(@ColorInt driverLineColor: Int): WheelViewBuilder {
        config.driverLineColor = driverLineColor
        return this
    }

    fun setItemNumber(itemNumber: Int): WheelViewBuilder {
        config.itemOffsetNumber = itemNumber
        return this
    }

    fun setSelectedTextColor(@ColorInt selectedTextColor: Int): WheelViewBuilder {
        config.selectedTextColor = selectedTextColor
        return this
    }

    fun setSelectedTextSize(selectedTextSize: Float): WheelViewBuilder {
        config.selectedTextSize = selectedTextSize
        return this
    }

    fun setUnselectedTextColor(@ColorInt unselectedTextColor: Int): WheelViewBuilder {
        config.unselectedTextColor = unselectedTextColor
        return this
    }

    fun setUnselectedTextSize(unselectedTextSize: Float): WheelViewBuilder {
        config.unselectedTextSize = unselectedTextSize
        return this
    }

    fun setItemPaddingLeft(itemPaddingLeft: Int): WheelViewBuilder {
        config.itemPaddingLeft = itemPaddingLeft
        return this
    }

    fun setItemPaddingRight(itemPaddingRight: Int): WheelViewBuilder {
        config.itemPaddingRight = itemPaddingRight
        return this
    }

    fun setItemSpace(itemSpace: Int): WheelViewBuilder {
        config.itemDistance = itemSpace
        return this
    }

    fun build(): IWheelViewConfig {
        return config
    }
}