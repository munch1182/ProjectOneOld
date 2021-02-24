package com.munch.project.launcher.app

import com.munch.lib.helper.AppHelper
import com.munch.project.launcher.base.App
import com.munch.project.launcher.bean.AppBean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Create by munch1182 on 2021/2/24 9:09.
 */
class AppRepository @Inject constructor() {


    fun queryAppByScan(): Flow<List<AppBean>?> {
        return flow {
            AppHelper.getInstallApp()
                ?.mapIndexed { index, it ->
                    it.iconResource
                    AppBean.new(
                        it.loadLabel(App.getInstance().packageManager).toString(),
                        it.iconResource.takeIf { it == 0 }?.toString(),
                        it.activityInfo.name,
                        it.activityInfo.packageName,
                        index
                    )
                }
        }
    }
}