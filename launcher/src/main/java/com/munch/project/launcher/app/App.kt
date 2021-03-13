package com.munch.project.launcher.app

import com.munch.lib.BaseApp
import com.munch.lib.helper.AppStatusHelper
import com.munch.lib.helper.ServiceBindHelper
import com.munch.lib.log
import com.munch.project.launcher.app.task.AppItemHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Create by munch1182 on 2021/2/23 15:04.
 */
@HiltAndroidApp
class App : BaseApp() {

    companion object {

        fun getInstance(): App = BaseApp.getInstance()
    }

    private val serviceHelper by lazy { ServiceBindHelper.bindApp<AppService>(this) }

    override fun onCreate() {
        super.onCreate()
        serviceHelper.bind()
        AppItemHelper.getInstance().registerReceiver(this)
        AppStatusHelper.register(this).getForegroundLiveData().observeForever {
            log(it)
        }
    }

}