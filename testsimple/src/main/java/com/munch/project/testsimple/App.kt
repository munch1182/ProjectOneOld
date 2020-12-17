package com.munch.project.testsimple

import android.app.Application
import androidx.work.Configuration

/**
 * Create by munch1182 on 2020/12/9 11:38.
 */
class App : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        app = this
    }

    companion object {
        private lateinit var app: App

        fun getInstance() = app
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }

}