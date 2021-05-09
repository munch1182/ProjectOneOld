package com.munch.project.launcher.item

import com.munch.pre.lib.dag.Executor
import com.munch.pre.lib.dag.Task
import kotlinx.coroutines.Dispatchers

/**
 * Create by munch1182 on 2021/5/9 14:40.
 */
class AppItemTask : Task() {
    override suspend fun start(executor: Executor) {
        AppItemHelper.preScan()
    }

    override val uniqueKey = "key_app_item"

    override val coroutineContext = Dispatchers.Default
}
