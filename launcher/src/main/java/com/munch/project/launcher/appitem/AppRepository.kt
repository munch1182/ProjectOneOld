package com.munch.project.launcher.appitem

import com.munch.lib.helper.AppHelper
import com.munch.project.launcher.app.App
import com.munch.project.launcher.db.AppBean
import com.munch.project.launcher.db.AppDao
import javax.inject.Inject

/**
 * Create by munch1182 on 2021/2/24 9:09.
 */
class AppRepository @Inject constructor(private val appDao: AppDao) {

    fun queryAppByScan(): List<AppBean>? {
        val pm = App.getInstance().packageManager ?: return null
        return AppHelper.getInstallApp()
            ?.filter { it.activityInfo.packageName != App.getInstance().packageName }
            ?.mapIndexed { index, it ->
                AppBean.new(
                    /*此方法可以获取被应用更改后的label*/
                    it.loadLabel(pm).toString(),
                    it.activityInfo.name,
                    it.activityInfo.packageName,
                    index,
                    /*此方法可以获取被应用更改后的icon*/
                    it.loadIcon(pm)
                )
            }
    }

    suspend fun queryAppByDb(): List<AppBean>? {
        return appDao.queryAll()
    }
}