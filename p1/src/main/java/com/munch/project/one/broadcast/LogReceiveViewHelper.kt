package com.munch.project.one.broadcast

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * 负责view的处理
 * Create by mucnh1182 on 2021/10/15 17:54.
 */
class LogReceiveViewHelper {


    companion object {

        val INSTANCE by lazy { LogReceiveViewHelper() }
    }


    private var isShowVal = false
    val isShow: Boolean
        get() = isShowVal

    fun start() {
        LogReceiveHelper.INSTANCE.onReceived = {

        }
    }

    fun stop() {
    }
}

class LogReceiveServer : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}