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
            //todo 此处不会执行，即可能会占用一个线程，因此考虑循环任务的方式，或者这样也可以
        }
    }
}