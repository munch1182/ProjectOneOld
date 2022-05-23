package com.munch.lib.result

import android.content.Intent
import androidx.fragment.app.Fragment
import com.munch.lib.result.request.*

/**
 * Created by munch1182 on 2022/4/10 3:58.
 */
class ResultFragment : Fragment(),
    PermissionRequest,
    IntentRequest,
    Judge2ResultRequest {

    private val permission: PermissionRequest = PermissionRequestHandler(this)
    private val intent: IntentRequest = IntentRequestHandler(this)
    private val judge: Judge2ResultRequest = Judge2ResultRequestHandler(this)

    override fun startIntent(intent: Intent, listener: OnIntentResultListener?) {
        this.intent.startIntent(intent, listener)
    }

    override fun judge2Result(judge: OnJudge, intent: Intent, listener: OnJudgeResultListener?) {
        this.judge.judge2Result(judge, intent, listener)
    }

    override fun requestPermissions(
        permissions: Array<out String>,
        notice: PermissionNotice?,
        listener: OnPermissionResultListener?
    ) {
        this.permission.requestPermissions(permissions, notice, listener)
    }

}