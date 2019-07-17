package com.munch.lib.test.app

import android.app.Application

/**
 * Created by Munch on 2019/7/16 9:08
 */
open class TestApp : Application() {


    companion object {

        private lateinit var INSTANCE: TestApp

        fun getInstance() = INSTANCE
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}