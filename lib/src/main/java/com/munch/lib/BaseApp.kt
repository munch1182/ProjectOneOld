package com.munch.lib

import android.app.Application

/**
 * Create by munch1182 on 2020/12/17 11:40.
 */
open class BaseApp : Application() {

    companion object {
        private lateinit var instance: Application

        @Suppress("UNCHECKED_CAST")
        fun <T> getInstance(): T = instance as T
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }


}