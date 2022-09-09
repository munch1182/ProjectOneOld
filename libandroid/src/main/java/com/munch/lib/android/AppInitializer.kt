package com.munch.lib.android

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class AppInitializer : Initializer<AppHelper> {
    override fun create(context: Context): AppHelper {
        AppHelper.init(context.applicationContext as Application)
        return AppHelper
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}