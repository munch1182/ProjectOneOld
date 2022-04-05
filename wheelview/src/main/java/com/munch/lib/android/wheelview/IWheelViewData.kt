package com.munch.lib.android.wheelview


/**
 * Created by munch1182 on 2022/4/3 23:22.
 */
interface IWheelViewData<T> {

    var curr: T

    /**
     * 参数value是否可见
     */
    fun isValueVisible(value: T): Boolean

    /**
     * 当前item的显示为curr，需要返回上一个item的显示内容
     *
     * @param curr 当前选中的值
     * @param offset 当前的偏移值，如-1表示上一个，1表示下一个
     */
    fun onValueOffset(curr: T, offset: Int): T

}

class DefaultWheelViewDataProvider : IWheelViewData<String> {

    private var min = 100
    private var max = 999
    override var curr = min.toString()

    override fun isValueVisible(value: String): Boolean {
        return value > min.toString() && value < max.toString()
    }

    override fun onValueOffset(curr: String, offset: Int): String {
        return (curr.toInt() + offset).toString()
    }

}