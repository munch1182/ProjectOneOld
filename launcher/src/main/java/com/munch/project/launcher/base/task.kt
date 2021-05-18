package com.munch.project.launcher.base

import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.dag.Executor
import com.munch.pre.lib.dag.Task
import com.munch.pre.lib.helper.receiver.AppInstallReceiver
import com.munch.project.launcher.extend.preLoad
import com.munch.project.launcher.item.AppItemHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/5/9 14:40.
 */
abstract class LogTask : Task() {

    protected val log = LauncherApp.appLog

    override suspend fun start(executor: Executor) {
        log.log("task ${this::class.java.simpleName} start")
    }
}

class AppItemTask : LogTask() {

    var update: (() -> Unit)? = null

    override suspend fun start(executor: Executor) {
        super.start(executor)
        AppItemHelper.getItems(true).forEach { it.info.icon?.preLoad() }
        AppInstallReceiver(BaseApp.getInstance())
            .apply {
                add { _, action, pkg ->
                    log.log("$pkg->$action")
                    if (AppInstallReceiver.isReplaced(action)) {
                        return@add
                    }
                    GlobalScope.launch(coroutineContext) {
                        AppItemHelper.getItems(true)
                    }
                    update?.invoke()
                }
            }
            .register()
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
