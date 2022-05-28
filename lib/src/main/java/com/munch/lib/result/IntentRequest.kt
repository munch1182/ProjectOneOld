package com.munch.lib.result

import android.content.Context
import android.content.Intent
import com.munch.lib.notice.Notice

/**
 * Created by munch1182 on 2022/5/28 2:19.
 */
class IntentRequest(private val intent: Intent, private val fragment: ResultFragment) :
    ResultHelper.IRequest {

    private var explain: ExplainIntentNotice? = null

    fun explain(explain: (Context) -> ExplainIntentNotice): IntentRequest {
        this.explain = explain.invoke(fragment.requireContext())
        return this
    }

    fun start(listener: OnIntentResultListener?) {
        fragment.startIntent(intent, listener)
    }
}

interface OnIntentResultListener {

    fun onIntentResult(isOk: Boolean, data: Intent?)
}

interface ExplainIntentNotice : Notice {

    fun onIntentExplain(intent: Intent): Boolean
}