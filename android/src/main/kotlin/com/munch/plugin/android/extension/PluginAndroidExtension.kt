package com.munch.plugin.android.extension

import org.gradle.api.provider.Property

abstract class PluginAndroidExtension {
    abstract fun getEnable(): Property<Boolean>

    abstract fun getLog(): Property<Boolean>

    fun initDefault() {
        getEnable().set(true)
        getLog().set(false)
    }
}