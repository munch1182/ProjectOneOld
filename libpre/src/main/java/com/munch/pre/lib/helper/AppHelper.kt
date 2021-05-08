package com.munch.pre.lib.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.os.storage.StorageManager
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.view.isVisible
import com.munch.pre.lib.DefaultDepend
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.extend.getAttrFromTheme
import com.munch.pre.lib.extend.getService
import com.munch.pre.lib.helper.file.FileHelper
import com.munch.pre.lib.log.log
import java.util.*
import kotlin.system.exitProcess

/**
 * Create by munch1182 on 2021/3/31 11:19.
 */
@DefaultDepend([BaseApp::class])
object AppHelper {

    val PARAMETER by lazy { PhoneParameter() }

    fun getVersionCodeAndName(context: Context = getBaseApp()): Pair<Long, String>? {
        val packageInfo = context.packageManager?.getPackageInfo(
            context.packageName,
            PackageManager.GET_CONFIGURATIONS
        ) ?: return null
        return Pair(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            },
            packageInfo.versionName
        )
    }

    /**
     * 获取安装的所有包，包括很多系统包
     *
     * 在android11，需要[android.Manifest.permission.QUERY_ALL_PACKAGES]权限
     *
     *@see [https://developer.android.google.cn/about/versions/11/privacy/package-visibility?authuser=0&hl=tr]
     *@see [https://developer.android.google.cn/training/basics/intents/package-visibility?authuser=0]
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun getInstallPackages(context: Context = getBaseApp()): MutableList<PackageInfo>? {
        return context.packageManager?.getInstalledPackages(PackageManager.GET_CONFIGURATIONS)
    }

    /**
     * 获取安装的用于打开的应用
     *
     * 在android11，需要[android.Manifest.permission.QUERY_ALL_PACKAGES]权限
     *
     * @param pkgName 目标app的包名，如果为null则返回权限内所有合适的app
     *
     * 即使只查一个包，也可能返回多个结果组成的list，因为一个app可能有多个入口
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun getInstallApp(
        context: Context = getBaseApp(),
        pkgName: String? = null
    ): MutableList<ResolveInfo>? {
        return context.packageManager?.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).apply {
                if (pkgName != null) {
                    setPackage(pkgName)
                }
            }, 0
        )
    }

    /**
     * 通过查找app信息是否成功来判断app是否安装
     */
    fun isAppInstall(context: Context = getBaseApp(), pkgName: String): Boolean {
        return try {
            context.packageManager?.getPackageInfo(pkgName, PackageManager.GET_GIDS)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isSystemApp(info: PackageInfo): Boolean {
        return (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1)
                || (info.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 1)
    }

    fun getAppIcon(context: Context = getBaseApp(), pkgName: String): Drawable? {
        try {
            return context.packageManager?.getApplicationIcon(pkgName) ?: return null
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 获取app大小相关，应用大小，缓存大小和文件大小
     *
     * @see checkUsagePermission
     * @see IntentHelper.usageIntent
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission("android.permission.PACKAGE_USAGE_STATS")
    fun getAppSize(context: Context = getBaseApp(), pkgName: String): StorageStats? {
        val storageStatsManager =
            context.getService<StorageStatsManager>(Context.STORAGE_STATS_SERVICE) ?: return null
        val storageManager =
            context.getService<StorageManager>(Context.STORAGE_SERVICE) ?: return null
        val uid = getUid(context, pkgName).takeIf { it != -1 } ?: return null
        storageManager.storageVolumes.forEach {
            try {
                val uuid =
                    if (it.uuid != null) UUID.fromString(it.uuid) else StorageManager.UUID_DEFAULT
                return storageStatsManager.queryStatsForUid(uuid, uid)
            } catch (e: Exception) {
                e.printStackTrace()
                log(e)
                return@forEach
            }
        }
        return null
    }

    /**
     * @see getAppSize
     * @see IntentHelper.usageIntent
     */
    @RequiresPermission("android.permission.PACKAGE_USAGE_STATS")
    fun checkUsagePermission(context: Context = getBaseApp()): Boolean {
        val opsManager = context.getService<AppOpsManager>(Context.APP_OPS_SERVICE) ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            opsManager.unsafeCheckOp(
                "android:get_usage_stats",
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            opsManager.checkOp("android:get_usage_stats", Process.myUid(), context.packageName)
        } == AppOpsManager.MODE_ALLOWED
    }

    fun getUid(context: Context = BaseApp.getInstance(), pkgName: String): Int {
        return try {
            context.packageManager?.getApplicationInfo(
                pkgName,
                PackageManager.GET_META_DATA
            )?.uid ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * 清除同一栈的Activity并启动[clazz]
     * 并没有重启app所以缓存的数据仍然有效
     * 注意对应activity的启动模式
     */
    fun resetApp2Activity(context: Context, clazz: Class<out Activity>, bundle: Bundle? = null) {
        context.startActivity(
            Intent(context, clazz)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                .apply {
                    if (bundle != null) {
                        putExtras(bundle)
                    }
                }
        )
    }

    /**
     * 清除同一栈的Activity并启动注册的启动activity，如果没有注册则什么都不做
     * 并没有重启app所以缓存的数据仍然有效
     * 注意启动activity的启动模式
     */
    fun resetApp2Activity(context: Context, bundle: Bundle? = null) {
        context.startActivity(
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                ?.apply {
                    if (bundle != null) {
                        putExtras(bundle)
                    }
                } ?: return)
    }

    fun closeApp() {
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    fun hideIm(activity: Activity) {
        val im = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?
            ?: return
        im.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
    }

    /**
     * 隐藏或者显示应用图标
     *
     * @param clazz ComponentClass，注意多启动图标应用传入不同的类会控制不同的图标
     * @param show 是否显示图标
     */
    fun showIcon(context: Context = getBaseApp(), clazz: Class<*>, show: Boolean) {
        val pm = context.packageManager ?: return
        pm.setComponentEnabledSetting(
            ComponentName(context, clazz),
            if (!show) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * 没有焦点或者没有显示或者布局未加载完成则此方法无效
     * @see View.requestFocus
     */
    fun showIm(view: EditText) {
        if (!(view.isFocusable && view.isVisible)) {
            return
        }
        val im = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?
            ?: return
        im.showSoftInput(view, 0)
    }

    fun put2Clip(context: Context = getBaseApp(), text: String) {
        val manager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager? ?: return
        manager.setPrimaryClip(ClipData.newPlainText("text", text))
    }

    fun uninstall(activity: Activity, pkgName: String, requestCode: Int = -1) {
        activity.startActivityForResult(
            Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkgName")),
            requestCode
        )
    }

    private fun getBaseApp(): Context = BaseApp.getInstance()

    @DefaultDepend([BaseApp::class])
    class PhoneParameter {
        private var screenWidthHeightReal: Size? = null
        private var screenWidthHeight: Size? = null
        private var statusBarHeight = -1
        private var navigationBarHeight = -1
        private var actionBarSize = -1

        fun getBrand(): String? = Build.BRAND

        fun getVersion() = Build.VERSION.SDK_INT

        fun getAbis(): Array<String> = Build.SUPPORTED_ABIS

        fun getMemoryInfo(context: Context = BaseApp.getInstance()): ActivityManager.MemoryInfo? {
            val manager =
                context.getService<ActivityManager>(Context.ACTIVITY_SERVICE) ?: return null
            val info = ActivityManager.MemoryInfo()
            manager.getMemoryInfo(info)
            return info
        }

        fun getRom(): StatFs {
            return StatFs(Environment.getDataDirectory().path)
        }

        /**
         * 分辨率
         */
        @Suppress("DEPRECATION")
        fun getScreenSize(context: Context = BaseApp.getInstance()): Size? {
            if (screenWidthHeightReal != null) {
                return screenWidthHeightReal
            }
            val point = Point()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.getRealSize(point)
            } else {
                val manager =
                    context.getService<WindowManager>(Context.WINDOW_SERVICE) ?: return null
                manager.defaultDisplay.getRealSize(point)
            }
            screenWidthHeightReal = Size(point.x, point.y)
            return screenWidthHeightReal
        }

        /**
         * 可见区域，一般是有底部虚拟导航栏的屏幕的高度减去导航栏高度
         */
        @Suppress("DEPRECATION")
        fun getScreenWidthHeight(context: Context = BaseApp.getInstance()): Size? {
            if (screenWidthHeight != null) {
                return screenWidthHeight
            }
            val manager = context.getService<WindowManager>(Context.WINDOW_SERVICE) ?: return null
            val width: Int
            val height: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = manager.currentWindowMetrics.bounds
                width = bounds.width()
                height = bounds.height()
            } else {
                val metrics = DisplayMetrics()
                manager.defaultDisplay.getMetrics(metrics)
                width = metrics.widthPixels
                height = metrics.heightPixels
            }
            screenWidthHeight = Size(width, height)
            return screenWidthHeight
        }

        fun getStatusBarHeight(context: Context = BaseApp.getInstance()): Int {
            if (statusBarHeight == -1) {
                statusBarHeight = getResById(context, "status_bar_height") ?: -1
            }
            return statusBarHeight
        }

        private fun getResById(context: Context, name: String): Int? {
            val resources = context.resources
            val id = resources.getIdentifier(name, "dimen", "android")
            return try {
                resources.getDimensionPixelSize(id)
            } catch (e: Resources.NotFoundException) {
                null
            }
        }

        fun getNavigationBarHeight(context: Context = BaseApp.getInstance()): Int {
            if (navigationBarHeight == -1) {
                navigationBarHeight = getResById(context, "navigation_bar_height") ?: -1
            }
            return navigationBarHeight
        }

        fun getActionBarSize(context: Context = BaseApp.getInstance()): Int {
            if (actionBarSize == -1) {
                actionBarSize = TypedValue.complexToDimensionPixelSize(
                    context.getAttrFromTheme(android.R.attr.actionBarSize).data,
                    context.resources.displayMetrics
                )
            }
            return actionBarSize
        }

        fun collect(context: Context = BaseApp.getInstance()): Array<Pair<String, String?>> {
            val memory = getMemoryInfo()
            val rom = getRom()
            return arrayOf(
                Pair("brand", getBrand()),
                Pair("sdk_version", getVersion().toString()),
                Pair("support abis", getAbis().toList().joinToString()),
                Pair(
                    "real_size",
                    "${getScreenSize(context)?.width}/${getScreenSize(context)?.height}"
                ),
                Pair(
                    "size",
                    "${getScreenWidthHeight(context)?.width}/${getScreenWidthHeight(context)?.height}"
                ),
                Pair("status bar height", "${getStatusBarHeight(context)}"),
                Pair("navigation bar height", "${getNavigationBarHeight(context)}"),
                Pair("action bar height", "${getActionBarSize(context)}"),
                Pair("ram", memory?.formatString()),
                Pair("rom", rom.formatString())
            )
        }

        private fun StatFs.formatString(): String {
            /*return "Rom(total:${FileHelper.formatSize2Str((availableBlocksLong * blockSizeLong).toDouble())},avail:${
                FileHelper.formatSize2Str((blockCountLong * blockSizeLong).toDouble())
            })"*/
            return "Rom:${FileHelper.formatSize2Str(availableBytes.toDouble())}/${
                FileHelper.formatSize2Str(totalBytes.toDouble())
            }"
        }

        private fun ActivityManager.MemoryInfo.formatString(): String {
            return "Ram:${FileHelper.formatSize2Str(availMem.toDouble())}/${
                FileHelper.formatSize2Str(totalMem.toDouble())
            }, isLow:${this.lowMemory}"
        }

        override fun toString(): String {
            val sep = ", "
            val sb = StringBuilder()
            collect().forEachIndexed { index, it ->
                if (index > 0) {
                    sb.append(sep)
                }
                sb.append(it.first).append(":").append(it.second)
            }
            return sb.toString()
        }
    }
}