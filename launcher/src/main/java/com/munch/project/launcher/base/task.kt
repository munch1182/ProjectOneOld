package com.munch.project.launcher.base

import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.dag.Executor
import com.munch.pre.lib.dag.Task
import com.munch.project.launcher.extend.preLoad
import com.munch.project.launcher.item.AppItemHelper
import kotlinx.coroutines.Dispatchers

/**
 * Create by munch1182 on 2021/5/9 14:40.
 */
abstract class LogTask : Task() {

    override suspend fun start(executor: Executor) {
        LauncherApp.appLog.log("task ${this::class.java.simpleName} start")
    }
}

class AppItemTask : LogTask() {
    override suspend fun start(executor: Executor) {
        super.start(executor)
        AppItemHelper.preScan()
        AppItemHelper.getItems().forEach { it.info.icon?.preLoad() }
    }

    override val uniqueKey = "key_app_item"
    override val coroutineContext = Dispatchers.Default
}

class DelayInitTask : LogTask() {
    override suspend fun start(executor: Executor) {
        super.start(executor)
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(BaseApp.getInstance())))
    }

    override val uniqueKey = "key_delay_init"
}
