package com.munch.project.testsimple.func

import android.content.Context
import android.content.Intent
import com.munch.lib.test.def.DefForegroundService

/**
 * Create by munch1182 on 2020/12/16 17:21.
 */
class TestForegroundService : DefForegroundService() {

    companion object {

        fun start(context: Context) {
            startForegroundService(
                context,
                Intent(context, TestForegroundService::class.java)
            )
        }
    }
}