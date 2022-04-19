package com.munch.project.one

import cn.munch.lib.DBRecord
import com.munch.lib.fast.BaseApp
import com.munch.lib.fast.notification.NotificationHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by munch1182 on 2022/4/19 23:10.
 */
class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        AppScope.launch {
            DBRecord.querySizeFlow().collectLatest {
                if (it > 500) {
                    NotificationHelper.getInstance().notify("record count $it")
                }
            }
        }
    }
}