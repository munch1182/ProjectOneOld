package com.munch.project.one.timer

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.project.one.databinding.ActivityTimerBinding
import com.munch.project.one.file.StringActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Create by munch1182 on 2022/2/22 16:08.
 */
class TimerActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityTimerBinding>()

    private val handler by lazy { HandlerTimer() }
    private val alarm by lazy { AlarmTimer() }
    private val workManager by lazy { WorkManagerTimer() }

    private val timers by lazy { mutableListOf(handler, alarm, workManager) }
    private val names by lazy { mutableListOf("HANDLER", "ALARM", "WORK MANAGER") }
    private var index = 0
        set(value) {
            field = if (value == timers.size) 0 else value
        }

    private var timer: ITimer? = null
        set(value) {
            field = value
            bind.timerType.text = if (value == null) "" else
                names[timers.indexOf(value).takeIf { it in 0 until timers.size } ?: 0]
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            timerType.setOnClickListener { changeType() }
            timerAdd.setOnClickListener { addTimer(false) }
            timerAddRepeat.setOnClickListener { addTimer(true) }
            timerStop.setOnClickListener { stopTimer() }
            timerClear.setOnClickListener { clear() }
            timerContent.setOnClickListener { showAll() }
            timerFile.setOnClickListener { showLog() }
        }
        timer = timers[index]

        showAll()
    }

    private fun showLog() {
        StringActivity.show(this, timer?.getFile()?.absolutePath ?: return)
    }

    private fun clear() {
        lifecycleScope.launch(Dispatchers.IO) {
            timer?.clear()

            showAll()
        }
    }

    private fun stopTimer() {
        clear()
    }

    private fun addTimer(isRepeat: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            timer?.add(Timer(isRepeat, 15 * 60 * 1000L))

            showAll()
        }
    }

    private fun showAll() {
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { bind.timerContent.text = "查询中..." }
            val str = timer?.query()?.joinToString { it.toString() }
            withContext(Dispatchers.Main) { bind.timerContent.text = str ?: "null" }
        }
    }

    private fun changeType() {
        index++
        timer = timers[index]
    }
}