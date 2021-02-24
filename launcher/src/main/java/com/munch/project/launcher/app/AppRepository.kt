package com.munch.project.launcher.app

import com.munch.lib.helper.AppHelper
import com.munch.project.launcher.base.App
import com.munch.project.launcher.db.AppBean
import javax.inject.Inject

/**
 * Create by munch1182 on 2021/2/24 9:09.
 */
class AppRepository @Inject constructor() {


    fun queryAppByScan(): List<AppBean>? {
        return AppHelper.getInstallApp()
            ?.mapIndexed { index, it ->
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