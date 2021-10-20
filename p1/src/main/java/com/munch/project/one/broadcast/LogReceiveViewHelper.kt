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
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.app.AppHelper
import com.munch.lib.fast.databinding.ItemLogContentBinding
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.lib.fast.recyclerview.setOnItemClickListener
import com.munch.lib.helper.PhoneHelper
import com.munch.project.one.R
import com.munch.project.one.data.DataHelper
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

class LogFloatViewHelper(context: Context) {

    private val binding = LayoutLogContentBinding.inflate(LayoutInflater.from(context))

    val root: View = binding.root
    private val simpleAdapter =
        SimpleAdapter<String, ItemLogContentBinding>(R.layout.item_log_content) { _, binding, str ->
            binding.text = str
        }

    init {
        binding.receiveLogReduce.setOnClickListener {
            if (binding.receiveLogItemTv.isShown) {
                showRvOrItem(false)
            } else {
                showReduceView()
            }
        }
        binding.receiveLogRv.layoutManager = LinearLayoutManager(context)
        binding.receiveLogRv.adapter = simpleAdapter
        simpleAdapter.setOnItemClickListener { _, pos, _ ->
            binding.receiveLogItemTv.text = simpleAdapter.data[pos]
            showRvOrItem(true)
        }
        DataHelper.LogReceive.getActions().forEach {
            binding.receiveLogFilter.addView(CheckBox(context).apply {
                text = it.action
                isChecked = it.isCheck
            })
        }
    }

    private fun showReduceView() {

    }

    private fun showRvOrItem(showItem: Boolean) {
        binding.receiveLogRv.visibility = if (showItem) View.INVISIBLE else View.VISIBLE
        binding.receiveLogItemTv.visibility = if (showItem) View.VISIBLE else View.INVISIBLE
    }

    fun updateView(content: LogBean) {
        binding.root.post { simpleAdapter.add(content.toStr(true)) }
    }

}

class LogReceiveServer : Service() {
    companion object {

        private const val FLAG_SHOW = "SHOW"
        private const val FLAG_DISMISS = "DISMISS"
        private const val FLAG_CONTENT = "CONTENT"
        private const val DATA_CONTENT = "DATA_CONTENT"

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
    private var floatView: LogFloatViewHelper? = null
    private val wh by lazy { PhoneHelper.getScreenWidthHeight() ?: Size(512, 450) }

    override fun onCreate() {
        super.onCreate()
        LogReceiveHelper.log.log("LogReceiveServer onCreate")
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        wm = windowManager
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
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
            return
        }
        floatView?.updateView(content)
    }

    private fun dismissView() {
        floatView?.root.let { wm.removeView(it) }
        floatView = null
        LogReceiveHelper.log.log("dismissView")
        isShowing = false
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogReceiveHelper.log.log("LogReceiveServer onDestroy")
    }

    private fun showView() {
        isShowing = true
        val lp = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.RGBA_8888 //背景透明
            width = wh.width
            height = wh.height / 2
            gravity = Gravity.BOTTOM or Gravity.START
            x = 0 //启动位置
            y = 0
        }
        floatView = LogFloatViewHelper(this)
        wm.addView(floatView!!.root, lp)
    }
}