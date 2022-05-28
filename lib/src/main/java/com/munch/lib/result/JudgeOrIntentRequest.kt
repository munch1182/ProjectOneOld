package com.munch.lib.result

import android.content.Context
import android.content.Intent
import com.munch.lib.log.Logger
import com.munch.lib.notice.choseSureOrFalse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class JudgeOrIntentRequest(
    private val judge: OnJudge,
    private val intent: Intent,
    private val fragment: ResultFragment,
) : ResultHelper.IRequest {

    private val log: Logger = Logger("JudgeIntent")
    private var explain: ExplainIntentNotice? = null
    private var time = 0L

    /**
     * 用于在第二次判断的时候进行延时
     *
     * 因为有些状态的判断需要延时
     */
    fun delay(time: Long): JudgeOrIntentRequest {
        this.time = time
        return this
    }

    fun explain(explain: (Context) -> ExplainIntentNotice): JudgeOrIntentRequest {
        this.explain = explain.invoke(fragment.requireContext())
        return this
    }

    fun start(listener: OnJudgeResultListener?) {
        val j = judge.invoke(fragment.requireContext())
        log.log { "first judge $intent: $j." }
        //第一次判断
        if (j) {
            listener?.onJudgeResult(true)
            return
        }

        fragment.launch(Dispatchers.Default) {
            val chose = choseFromNotice()
            //如果显示了notice并选择了false，则直接回调结果
            if (!chose) {
                log.log { "chose cancel, onJudgeResult(false)." }
                listener?.onJudgeResult(false)
                // 否则开始请求intent
            } else {
                intentRequest()
                log.log { "intent request over." }
                if (time > 0) {
                    delay(time)
                }
                val j2 = judge.invoke(fragment.requireContext())
                log.log { "second judge $intent: $j2." }

                listener?.onJudgeResult(j2)
            }
        }
    }

    private suspend fun intentRequest() = suspendCancellableCoroutine<Boolean> {
        IntentRequest(intent, fragment).start { _, _ -> it.resume(true) }
    }

    private suspend fun choseFromNotice(): Boolean {
        val e = explain
        if (e == null || !e.onIntentExplain(intent)) {
            return true
        }
        return e.choseSureOrFalse()
    }
}

typealias OnJudge = (Context) -> Boolean
typealias OnIntent = (Context) -> Intent

interface OnJudgeResultListener {

    fun onJudgeResult(result: Boolean)
}