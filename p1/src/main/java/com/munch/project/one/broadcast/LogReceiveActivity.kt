package com.munch.project.one.broadcast

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.munch.lib.app.AppHelper
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.result.ResultHelper
import com.munch.project.one.databinding.ActivityLogReceiveBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateState()
        showByState()
        bind.receiveBtn.setOnClickListener {
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
                bind.receiveBtn.text = "申请悬浮权限"
            }
            State.NOT_OPEN -> {
                bind.receiveBtn.text = "收集日志"
            }
            State.RUNNING_BACKGROUND -> {
                bind.receiveBtn.text = "关闭收集"
            }
            State.SHOWING -> {
                bind.receiveBtn.text = "关闭收集"
            }
        }

    }

    private fun stop() {
        LogReceiveHelper.INSTANCE.stop()
        LogReceiveViewHelper.INSTANCE.stop()
    }

    private fun start() {
        LogReceiveHelper.INSTANCE.start()
        LogReceiveViewHelper.INSTANCE.start()
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