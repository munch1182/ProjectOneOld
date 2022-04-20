package com.munch.project.one.notification

import android.content.Context
import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ISupportActionBar
import com.munch.lib.helper.NotificationServiceHelper
import com.munch.lib.result.OnJudge
import com.munch.lib.result.OnJudgeResultListener
import com.munch.lib.result.ResultHelper
import com.munch.lib.task.postUI

/**
 * Created by munch1182 on 2022/4/20 20:13.
 */
class NotificationActivity : BaseFastActivity(), ISupportActionBar {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO UI
        NotificationServiceHelper.set(this) { _, _ ->

        }

        postUI(1000L) {
            ResultHelper.with(this)
                .judge(object : OnJudge {
                    override fun onJudge(context: Context): Boolean {
                        return NotificationServiceHelper.isEnable
                    }
                }, NotificationServiceHelper.request)
                .result(object : OnJudgeResultListener {
                    override fun onJudgeResult(result: Boolean) {
                    }
                })
        }
    }
}