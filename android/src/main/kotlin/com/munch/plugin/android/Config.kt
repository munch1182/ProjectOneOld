package com.munch.plugin.android

object Config {

    var enable = true

    var log = false

    // 使用的包名限定
    var packName = "com.munch"

    override fun toString(): String {
        return "Config(enable=$enable, log=$log, packName=$packName)"
    }
}