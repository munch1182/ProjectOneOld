package com.munch.project.launcher.item

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.file.FileHelper
import com.munch.project.launcher.base.LauncherApp
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2021/5/9 14:41.
 */
object AppItemHelper : CoroutineScope {

    private val log = LauncherApp.appLog
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job

    private var currentAsync: Deferred<MutableList<AppItem>>? = null
    private val lock = Mutex()


    private fun queryItemsAsync(): Deferred<MutableList<AppItem>> {
        return async {
            log.log("app item querying")
            val hide = queryHide()
            val freeze = queryFreeze()
            val context = BaseApp.getInstance()
            ensureActive()
            val apps = AppHelper.getInstallApp(context) ?: return@async mutableListOf<AppItem>()
            ensureActive()
            log.log("app item be queried")
            return@async MutableList(apps.size) {
                val info = apps[it]
                val appInfo = AppInfo.from(info, context.packageManager)
                val key = appInfo.hashCode()
                val prop = AppProp.from(hide.contains(key), freeze.contains(key))
                AppItem(key, appInfo, prop)
            }
        }
    }

    /**
     * @param reload 是否要重新获取，而不是从缓存中获取
     */
    suspend fun getItems(reload: Boolean = false): MutableList<AppItem> {
        lock.withLock {
            //如果需要重新获取
            if (reload) {
                //如果上一次任务未完成，则取消该任务
                if (currentAsync?.isCompleted == false) {
                    currentAsync?.cancel()
                    log.log("cancel query")
                }
                currentAsync = null
            }
            //如果是第一次查询或者要重新查询
            if (currentAsync == null) {
                currentAsync = queryItemsAsync()
            }
        }
        return currentAsync?.await() ?: mutableListOf()
    }

    private fun queryFreeze(): MutableList<Int> {
        return mutableListOf()
    }

    private fun queryHide(): MutableList<Int> {
        return mutableListOf()
    }
}

data class AppItem(
    val key: Int,
    val info: AppInfo,
    val prop: AppProp
) : Comparable<AppItem> {
    override fun compareTo(other: AppItem): Int {
        return info.compareTo(other.info)
    }
}

data class AppProp(
    //是否隐藏显示
    var isHide: Boolean = false,
    //是否冻结
    var isFreeze: Boolean = false
) {

    companion object {

        fun from(isHide: Boolean, isFreeze: Boolean) = AppProp(isHide, isFreeze)
    }
}

data class AppInfo(
    val name: String,
    var showName: String,
    var icon: Any?,
    var showIcon: Any?,
    //包名
    val pkgName: String,
    val launch: String,
    //文件大小
    var size: String = "0",
    //是否是系统应用
    val isSystem: Boolean = false,
    //安装时间
    val installTime: Long,
    //上一次更新时间
    var lastUpdateTime: Long
) : Comparable<AppInfo> {

    companion object {

        fun from(info: ResolveInfo, pm: PackageManager): AppInfo {
            val packageName = info.activityInfo.packageName
            val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS)
            val name = pm.getApplicationLabel(packageInfo.applicationInfo).toString()
            val showName = info.activityInfo.loadLabel(pm).toString()
            val launch = info.activityInfo.name
            val icon = packageInfo.applicationInfo.loadIcon(pm)
            val showIcon = info.loadIcon(pm)
            val size = getSize(packageName)
            val isSystem = AppHelper.isSystemApp(packageInfo)
            val installTime = packageInfo.firstInstallTime
            val lastUpdateTime = packageInfo.lastUpdateTime
            return AppInfo(
                name, showName, icon, showIcon, packageName, launch,
                size, isSystem, installTime, lastUpdateTime
            )
        }

        private fun getSize(pkgName: String): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && AppHelper.checkUsagePermission()) {
                val appSize = AppHelper.getAppSize(BaseApp.getInstance(), pkgName) ?: return "0"
                val size = FileHelper.formatSize(appSize.appBytes.toDouble())
                return "${size.first.split(".")[0]} ${size.second}"
            }
            return "0"
        }
    }

    override fun compareTo(other: AppInfo): Int {
        return name.compareTo(other.name)
    }

    override fun hashCode(): Int {
        return pkgName.hashCode() * 31 + showName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other?.hashCode()
    }
}