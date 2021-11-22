package com.munch.project.one.broadcast

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Size
import android.view.*
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.app.AppHelper
import com.munch.lib.fast.databinding.ItemLogContentBinding
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.lib.fast.recyclerview.setOnItemClickListener
import com.munch.lib.helper.PhoneHelper
import com.munch.lib.task.ThreadHandler
import com.munch.project.one.R
import com.munch.project.one.databinding.LayoutLogContentBinding
import java.io.Closeable
import kotlin.math.absoluteValue

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

@SuppressLint("ClickableViewAccessibility")
class LogFloatViewHelper(context: Context) : Closeable {

    private val binding = LayoutLogContentBinding.inflate(LayoutInflater.from(context))
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val root: View = binding.root
    private val simpleAdapter =
        SimpleAdapter<LogBean, ItemLogContentBinding>(R.layout.item_log_content) { _, binding, b ->
            binding.text = b?.toStr()
        }
    private val wh by lazy { PhoneHelper.getScreenWidthHeight() ?: Size(512, 450) }
    private val lp by lazy {
        WindowManager.LayoutParams().apply {
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
    }
    private val moveHandler by lazy { ThreadHandler("MOVE_FLOAT") }
    private val gestureDetector by lazy {
        GestureDetector(context, object :
            GestureDetector.SimpleOnGestureListener() {

            private var alpha = 69

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (distanceX.absoluteValue > distanceY.absoluteValue) {
                    alpha += if (distanceX > 0) 1 else -1
                    root.setBackgroundColor(Color.argb(alpha, 255, 255, 255))
                } else {
                    lp.y += distanceY.toInt()
                    updateWM()
                }
                return true
            }

        }, moveHandler)
    }
    private var added = false

    init {
        showExpandView()
        binding.receiveLogRv.layoutManager = LinearLayoutManager(context)
        binding.receiveLogRv.adapter = simpleAdapter
        simpleAdapter.setOnItemClickListener { _, pos, _ ->
            binding.receiveLogItemTv.text =
                simpleAdapter.data[pos]?.let { "${it.from}:\n\t${it.toStr(true)}" }
            showRvOrItem(true)
        }
        binding.receiveLogItemTv.setOnClickListener { showRvOrItem(false) }
        root.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    private fun showExpandView() {
        binding.receiveLogReduce.setOnClickListener {
            /*if (binding.receiveLogItemTv.isShown) {
                showRvOrItem(false)
            } else {*/
            showReduceView()
            /*}*/
        }
        binding.receiveLogReduce.text = "-"
        binding.receiveLogContainer.children.forEach {
            if (it != binding.receiveLogItemTv) {
                it.visibility = View.VISIBLE
            }
        }

        lp.width = wh.width
        lp.height = wh.height / 2
        updateWM()
    }

    private fun updateWM() {
        if (added) {
            wm?.updateViewLayout(root, lp)
        } else {
            wm?.addView(root, lp)
            added = true
        }
    }

    private fun showReduceView() {
        binding.receiveLogReduce.text = "+"
        binding.receiveLogReduce.setOnClickListener { showExpandView() }
        binding.receiveLogContainer.children.forEach {
            if (it != binding.receiveLogReduce) {
                it.visibility = View.GONE
            }
        }
        lp.width = binding.receiveLogReduce.width
        lp.height = binding.receiveLogReduce.height
        updateWM()
    }

    private fun showRvOrItem(showItem: Boolean) {
        binding.receiveLogRv.visibility = if (showItem) View.INVISIBLE else View.VISIBLE
        binding.receiveLogItemTv.visibility = if (showItem) View.VISIBLE else View.INVISIBLE
    }

    fun updateView(content: LogBean) {
        binding.root.post { simpleAdapter.add(content) }
    }

    override fun close() {
        if (added) {
            wm?.removeView(binding.root)
        }
        moveHandler.quit()
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

    private var floatView: LogFloatViewHelper? = null

    override fun onCreate() {
        super.onCreate()
        LogReceiveHelper.log.log("LogReceiveServer onCreate")
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
        floatView?.close()
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
        floatView = LogFloatViewHelper(this)
        /*wm.addView(floatView!!.root, lp)*/
    }
}