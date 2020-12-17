package com.munch.project.testsimple

import android.os.Build
import androidx.work.Configuration
import com.munch.lib.BaseApp
import com.munch.lib.helper.ForegroundHelper
import com.munch.lib.helper.stopAllService
import com.munch.project.testsimple.alive.foreground.ForegroundService
import dagger.hilt.android.HiltAndroidApp

/**
 * Create by munch1182 on 2020/12/9 11:38.
 */
@HiltAndroidApp
class App : BaseApp(), Configuration.Provider {

    companion object {
        fun getInstance() = getInstance<App>()
    }

    override fun onCreate() {
        super.onCreate()
        ForegroundHelper.register(this).getForegroundLiveData().observeForever {
            if (!it) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ForegroundService.start(this)
                }
            } else {
                stopAllService()
            }
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }

}