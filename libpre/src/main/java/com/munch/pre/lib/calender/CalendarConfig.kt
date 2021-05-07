package com.munch.pre.lib.calender

import java.util.*


/**
 * Create by munch1182 on 2021/5/7 11:38.
 */
data class CalendarConfig(
    var firstDayOfWeek: Int = Calendar.MONDAY,
    val wh: WH = WH(),
    val height: Height = Height(),
    var max: Day = Day(2999, 12, 31), var min: Day = Day(999, 1, 1)
) {

    data class WH(
        var minWidth: Int = -1,
        var maxWidth: Int = -1,
        var minHeight: Int = -1,
        var maxHeight: Int = -1,
        var width: Int = 80,
        var height: Int = 80,
        var borderWidth: Int = 1
    )

    data class Height(
        //固定高度
        //当为-1时随着每月周数变化
        //否则固定几周显示
        //最大不超过6
        var fixHeight: Int = -1,
        //当固定显示几周时，从此周开始显示
        var startWeek: Int = 1,
        //显示前后月份的数据来填满
        var showNear: Boolean = false,
    )
}