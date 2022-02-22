package com.munch.project.one.timer

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.project.one.databinding.ActivityTimerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Create by munch1182 on 2022/2/22 16:08.
 */
class TimerActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityTimerBinding>()

    private val handler by lazy { HandlerTimer() }

    private var timer: ITimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.timerType.setOnClickListener { changeType() }
        bind.timerAdd.setOnClickListener { addTimer(false) }
        bind.timerAddRepeat.setOnClickListener { addTimer(true) }
        bind.timerStop.setOnClickListener { stopTimer() }
        bind.timerClear.setOnClickListener { clear() }
        bind.timerContent.setOnClickListener { showAll() }
        timer = handler

        showAll()
    }

    private fun clear() {
        lifecycleScope.launch(Dispatchers.IO) { timer?.clear() }
    }

    private fun stopTimer() {

    }

    private fun addTimer(isRepeat: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            timer?.add(Timer(isRepeat, 1 * 60 * 1000L))

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
    }
}