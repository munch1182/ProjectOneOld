package com.munch.project.one.broadcast

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.app.AppHelper
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.DataHelper
import com.munch.lib.fast.dialog.SimpleEditDialog
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.lib.result.ResultHelper
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityLogReceiveBinding
import com.munch.project.one.databinding.ItemLogReceiveBinding

/**
 * Create by munch1182 on 2021/10/14 17:55.
 */
class LogReceiveActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityLogReceiveBinding>()

    @State
    private var state: Int = State.NO_PERMISSION
        set(value) {
            if (field != value) {
                field = value
                showByState()
            }
        }
    private val logAdapter =
        SimpleAdapter<LogActionVB, ItemLogReceiveBinding>(R.layout.item_log_receive) { _, b, bean ->
            b.action = bean
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind.receiveLogRv.apply {
            layoutManager = LinearLayoutManager(this@LogReceiveActivity)
            adapter = logAdapter
        }
        bind.receiveLogAdd.setOnClickListener {
            SimpleEditDialog(this)
                .setOnTextListener { dialog, text ->
                    dialog.cancel()
                    if (text.isNotEmpty()) {
                        logAdapter.add(LogActionVB(text))
                    }
                }
                .show()
        }
        updateState()
        showByState()
        bind.receiveLogBtn.setOnClickListener {
            when (state) {
                State.NO_PERMISSION ->
                    ResultHelper.init(this)
                        .with({ hasPermission }, overlayIntent)
                        .startOk { updateState() }
                State.NOT_OPEN -> start()
                State.RUNNING_BACKGROUND, State.SHOWING -> stop()
            }
        }
    }

    private fun showByState() {
        when (state) {
            State.NO_PERMISSION -> {
                bind.receiveLogBtn.text = "申请悬浮权限"
            }
            State.NOT_OPEN -> {
                bind.receiveLogBtn.text = "开始收集日志"
            }
            State.RUNNING_BACKGROUND -> {
                bind.receiveLogBtn.text = "关闭日志收集"
            }
            State.SHOWING -> {
                bind.receiveLogBtn.text = "关闭日志收集"
            }
        }

    }

    private fun stop() {
        LogReceiveHelper.INSTANCE.stop()
        LogReceiveViewHelper.INSTANCE.stop()
    }

    private fun start() {
        val a = logAdapter.data
        if (a.isEmpty()) {
            toast("请先添加Actions")
            return
        }
        LogReceiveHelper.INSTANCE.start(*actions)
        LogReceiveViewHelper.INSTANCE.start()
    }

    private val actions: Array<String>
        get() {
            val str = DataHelper.App.instance.get("log_receive_actions", "") ?: ""
            if (str.isEmpty()) {
                return arrayOf()
            }
            return str.split(";").toTypedArray()
        }

    private fun putActions(actions: Array<String>) {
        val sb = StringBuilder()
        actions.forEach { sb.append(it).append(";") }
        DataHelper.App.instance.put("log_receive_actions", sb.toString())
    }

    private fun updateState() {
        state = judgeState()
    }

    @State
    private fun judgeState(): Int {
        if (!hasPermission) {
            return State.NO_PERMISSION
        }
        if (!LogReceiveHelper.INSTANCE.isRunning) {
            return State.NOT_OPEN
        }
        if (!LogReceiveViewHelper.INSTANCE.isShow) {
            return State.RUNNING_BACKGROUND
        }
        return State.SHOWING
    }

    private val hasPermission: Boolean
        get() = Settings.canDrawOverlays(AppHelper.app)

    private val overlayIntent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${AppHelper.app.packageName}")
        )

    @Retention(AnnotationRetention.SOURCE)
    annotation class State {
        companion object {

            const val NO_PERMISSION = 0
            const val NOT_OPEN = 1
            const val RUNNING_BACKGROUND = 2
            const val SHOWING = 3
        }
    }

}

data class LogActionVB(val action: String, var isCheck: Boolean = true) {

}