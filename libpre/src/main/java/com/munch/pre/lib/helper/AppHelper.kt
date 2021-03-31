package com.munch.pre.lib.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.munch.pre.lib.DefaultDepend
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.extend.getAttrFromTheme
import com.munch.pre.lib.extend.getService

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
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun getInstallApp(context: Context = getBaseApp()): MutableList<ResolveInfo>? {
        return context.packageManager?.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
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

    fun getAppIcon(context: Context = BaseApp.getInstance(), pkgName: String): Drawable? {
        try {
            return context.packageManager?.getApplicationIcon(pkgName) ?: return null
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 通过查找返回app信息，相较于其它方式能获取更多信息
     *
     * 有些版本需要权限
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun getAppShowIcon(
        context: Context = BaseApp.getInstance(),
        pkgName: String
    ): MutableList<ResolveInfo> {
        return context.packageManager?.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkgName), 0
        )?.takeIf { it.isNotEmpty() } ?: mutableListOf()
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

    fun hideIm(activity: Activity) {
        val im = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?
            ?: return
        im.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
    }

    fun showIm(view: View) {
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
                Pair("action bar height", "${getActionBarSize(context)}")
            )
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