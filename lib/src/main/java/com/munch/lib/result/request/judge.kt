package com.munch.lib.result.request

import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.munch.lib.Resettable
import com.munch.lib.log.Logger
import com.munch.lib.result.OnJudge
import com.munch.lib.result.OnJudgeResultListener

/**
 * Created by munch1182 on 2022/4/10 4:18.
 */
interface Judge2ResultRequest {
    fun judge2Result(judge: OnJudge, intent: Intent, listener: OnJudgeResultListener?)
}


class Judge2ResultRequestHandler(fragment: Fragment) : Judge2ResultRequest, Resettable,
    ActivityResultCaller by fragment {

    private val log = Logger("jud2Res")
    private val context by lazy { fragment.requireContext() }
    private var judge: OnJudge? = null
    private var listener: OnJudgeResultListener? = null
    private val onResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            judge?.let {
                val result = it.onJudge(context)
                log.log("receive result, judge result again: $result.")
                listener?.onJudgeResult(result)
            }
            reset()
        }

    override fun judge2Result(judge: OnJudge, intent: Intent, listener: OnJudgeResultListener?) {
        reset()
        if (judge.onJudge(context)) {
            log.log("judge result: true.")
            listener?.onJudgeResult(true)
        } else {
            this.judge = judge
            this.listener = listener
            log.log("judge result: false, launch intent.")
            onResultLauncher.launch(intent)
        }
    }

    override fun reset() {
        judge = null
        listener = null
    }
}