package com.munch.project.launcher.base

import com.munch.lib.BaseApp
import dagger.hilt.android.HiltAndroidApp

/**
 * Create by munch1182 on 2021/2/23 15:04.
 */
@HiltAndroidApp
class App : BaseApp() {

    companion object {

        fun getInstance(): App = BaseApp.getInstance()
    }
}