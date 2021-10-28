package com.munch.project.one.timer

import android.os.Bundle
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.project.one.databinding.ActivityTimerBinding

/**
 * Create by munch1182 on 2021/10/28 10:48.
 */
class WorkManagerActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityTimerBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

class WMHelper : ITimer {

    override fun add(timer: Timer): Boolean {
        TODO("Not yet implemented")
    }

    override fun del(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun query(): MutableList<Timer> {
        TODO("Not yet implemented")
    }
}