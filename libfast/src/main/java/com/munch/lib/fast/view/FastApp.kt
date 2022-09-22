package com.munch.lib.fast.view

import android.app.Application
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.munch.lib.android.helper.ThreadHelper
import com.munch.lib.fast.view.base.ActivityHelper
import java.io.InputStream

class FastApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ActivityHelper.register()
        ThreadHelper.setCaught(Uncaught()).caughtThreadException()
    }
}