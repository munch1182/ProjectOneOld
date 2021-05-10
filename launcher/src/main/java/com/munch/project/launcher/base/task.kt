package com.munch.project.launcher.base

import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.dag.Executor
import com.munch.pre.lib.dag.Task
import com.munch.project.launcher.item.AppItemHelper
import kotlinx.coroutines.Dispatchers

/**
 * Create by munch1182 on 2021/5/9 14:40.
 */
class AppItemTask : Task() {
    override suspend fun start(executor: Executor) {
        LauncherApp.appLog.log("app item task start")
        AppItemHelper.preScan()
    }

    override val uniqueKey = "key_app_item"

    override val coroutineContext = Dispatchers.Default
}

class DelayInitTask : Task() {
    override suspend fun start(executor: Executor) {
        LauncherApp.appLog.log("delay init task start")
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(BaseApp.getInstance())))
    }

    override val uniqueKey = "key_delay_init"
    override val coroutineContext = Dispatchers.Default
}
