package com.munch.lib.fast

import android.app.Application
import android.content.Context
import com.munch.lib.AppHelper
import com.munch.lib.fast.measure.MeasureHelper
import com.munch.lib.helper.ActivityHelper
import com.munch.lib.helper.data.MMKVHelper
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/4/15 21:20.
 */
open class BaseApp : Application() {

    override fun attachBaseContext(base: Context?) {
        MeasureHelper.start(MeasureHelper.KEY_LAUNCHER)
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        AppHelper.init(this)
        ActivityHelper.register()
        MMKVHelper.init()
    }

    object AppScope : CoroutineScope {
        override val coroutineContext: CoroutineContext = CoroutineName("app")
    }
}