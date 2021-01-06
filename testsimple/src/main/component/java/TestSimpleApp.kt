package com.munch.project.testsimple

import androidx.work.Configuration
import com.munch.lib.BaseApp
import dagger.hilt.android.HiltAndroidApp

/**
 * Create by munch1182 on 2020/12/9 11:38.
 */
@HiltAndroidApp
class TestSimpleApp : BaseApp(), Configuration.Provider {

    companion object {
        fun getInstance() = getInstance<TestSimpleApp>()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }

}