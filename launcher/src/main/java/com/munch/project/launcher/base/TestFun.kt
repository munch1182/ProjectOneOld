package com.munch.project.launcher.base

import com.munch.pre.lib.helper.measure.MeasureTimeHelper
import com.munch.pre.lib.helper.measure.SimpleMeasureTime
import com.munch.pre.lib.log.Logger

/**
 * Create by munch1182 on 2021/5/8 17:47.
 */
interface TestFun {

    val appLog : Logger
        get() = LauncherApp.appLog
    val measureHelper: SimpleMeasureTime
        get() = LauncherApp.measureHelper
}