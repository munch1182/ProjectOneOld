package com.munch.plugin.android

object Config {

    var enable = true

    var logDebug = false

    var tagCost = "cost-loglog"
    var tagCall = "call-loglog"
    var minTime = 500L

    // 使用的包名限定
    var packName = arrayOf("com.munch")

    override fun toString(): String {
        return "Config(enable=$enable, logDebug=$logDebug, tagCost=$tagCost, tagMeasure=$tagCall, minTime=$minTime, packName=${packName.joinToString()})"
    }
}