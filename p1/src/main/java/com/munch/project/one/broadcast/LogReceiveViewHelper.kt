package com.munch.project.one.broadcast

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.munch.lib.app.AppHelper
import com.munch.lib.helper.PhoneHelper
import com.munch.lib.log.log
import com.munch.project.one.databinding.LayoutLogContentBinding

/**
 * 负责view的处理
 * Create by munch1182 on 2021/10/15 17:54.
 */
class LogReceiveViewHelper {


    companion object {

        val INSTANCE by lazy { LogReceiveViewHelper() }
    }


    val isShow: Boolean
        get() = LogReceiveServer.isShowing

    fun start() {
        LogReceiveServer.show()
        LogReceiveHelper.INSTANCE.onReceived = { LogReceiveServer.update(it) }
    }

    fun stop() {
        LogReceiveServer.dismiss()
    }
}

class LogReceiveServer : Service() {
    companion object {

        private const val FLAG_SHOW = "SHOW"
        private const val FLAG_DISMISS = "DISMISS"
        private const val FLAG_CONTENT = "CONTENT"
        private const val DATA_CONTENT = "CONTENT"

        internal var isShowing = false

        fun show() = startCommand(FLAG_SHOW)
        fun dismiss() = startCommand(FLAG_DISMISS)
        fun update(content: LogBean) = startCommand(FLAG_CONTENT, content)

        private fun startCommand(actionStr: String, content: LogBean? = null) {
            AppHelper.app.startService(Intent(AppHelper.app, LogReceiveServer::class.java).apply {
                action = actionStr
                content?.let { this.putExtra(DATA_CONTENT, it) }
            })
        }
    }

    private lateinit var wm: WindowManager
    private var floatView: View? = null
    private val wh by lazy { PhoneHelper.getScreenWidthHeight() ?: Size(512, 450) }

    override fun onCreate() {
        super.onCreate()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        wm = windowManager
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        log("action:$action")
        when (action) {
            FLAG_SHOW -> showView()
            FLAG_DISMISS -> dismissView()
            FLAG_CONTENT -> intent.getParcelableExtra<LogBean>(DATA_CONTENT)?.let { updateView(it) }
            else -> {
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateView(content: LogBean) {
        if (!isShowing) {
            showView()
        }
    }

    private fun dismissView() {
        floatView?.let { wm.removeView(it) }
        log("dismissView")
        isShowing = false
        stopSelf()
    }

    @Suppress("DEPRECATION")
    private fun showView() {
        isShowing = true
        val lp = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.RGBA_8888 //背景透明
            width = wh.width
            height = wh.height / 2
            gravity = Gravity.BOTTOM
            x = 0 //启动位置
            y = height
        }
        val binding = LayoutLogContentBinding.inflate(LayoutInflater.from(this))
        floatView = binding.root
        log("showView:$wh")
        wm.addView(binding.root, lp)
    }
}