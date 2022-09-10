package com.munch.plugin.android.extension

import org.gradle.api.provider.Property

abstract class PluginAndroidExtension {
    abstract fun getEnable(): Property<Boolean>

    abstract fun getLogDebug(): Property<Boolean>

    abstract fun getTagCost(): Property<String>
    abstract fun getTagCall(): Property<String>

    abstract fun getMinTime(): Property<Long>

    abstract fun getPackName(): Property<Array<String>>
}