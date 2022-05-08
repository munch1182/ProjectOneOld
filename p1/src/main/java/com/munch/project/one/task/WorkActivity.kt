package com.munch.project.one.task

import androidx.work.Worker
import androidx.work.WorkerParameters
import com.munch.lib.AppHelper
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef

/**
 * Created by munch1182 on 2022/5/8 15:57.
 */
class WorkActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    class TestWorker(params: WorkerParameters) : Worker(AppHelper.app, params) {
        override fun doWork(): Result {
            inputData
            return Result.success()
        }
    }
}