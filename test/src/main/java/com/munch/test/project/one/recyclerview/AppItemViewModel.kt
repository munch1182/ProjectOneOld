package com.munch.test.project.one.recyclerview

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.promeg.pinyinhelper.Pinyin
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.extend.formatDate
import com.munch.pre.lib.extend.getService
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.file.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.*
import kotlin.collections.HashMap

/**
 * Create by munch1182 on 2021/4/9 15:36.
 */
open class AppItemViewModel : ViewModel() {

    private val appItems by lazy {
        flow {
            val context = BaseApp.getInstance()
            val pm = context.packageManager ?: return@flow
            val map = getUsage(context)
            val apps = AppHelper.getInstallApp() ?: return@flow
            emit(MutableList(apps.size) {
                val i = apps[it]
                val pkgName = i.activityInfo.packageName
                val info = pm.getPackageInfo(pkgName, PackageManager.GET_CONFIGURATIONS)
                val size = getSize(context, pkgName)
                AppItem(
                    i.loadLabel(pm)?.toString() ?: "null",
                    i.loadIcon(pm),
                    pkgName,
                    size,
                    info.firstInstallTime,
                    info.lastUpdateTime,
                    map?.get(pkgName) ?: -1L
                )
            })
        }
    }

    private fun getSize(context: BaseApp, pkgName: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appSize = AppHelper.getAppSize(context, pkgName) ?: return "0"
            val size = FileHelper.formatSize(appSize.appBytes.toDouble())
            return "${size.first.split(".")[0]} ${size.second}"
        }
        return "0"
    }

    private fun getUsage(context: Context): HashMap<String, Long>? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usage =
                context.getService<UsageStatsManager>(Context.USAGE_STATS_SERVICE) ?: return null
            val currentTimeMillis = System.currentTimeMillis()
            val usageStats = usage.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                currentTimeMillis - 24L * 60L * 60L * 1000L * 7, currentTimeMillis
            )
            if (!usageStats.isNullOrEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val map = HashMap<String, Long>(usageStats.size)
                    usageStats.forEach {
                        map[it.packageName] = it.totalTimeVisible
                    }
                    return map
                }
            }
        }
        return null
    }

    val appItemsSortByLetter by lazy {
        appItems.map {
            if (it.isEmpty()) {
                return@map it
            }
            it.sort()
            var last = it[0].char
            it.forEach { app ->
                if (app.char == last) {
                    app.isHeader = false
                } else {
                    app.isHeader = true
                    last = app.char
                }
            }
            it[0].isHeader = true
            it
        }.asLiveData(viewModelScope.coroutineContext + Dispatchers.Default)
    }


    data class AppItem(
        val name: String,
        val icon: Any?,
        val pkgName: String,
        val size: String = "0",
        val installTime: Long,
        val lastUpdateTime: Long,
        val useTimeIn7: Long
    ) : Comparable<AppItem>, HeaderItemDecoration.IsHeader {

        internal var isHeader: Boolean = false

        private var letter: String = ""
            get() {
                if (field.isEmpty()) {
                    val sb = StringBuilder()
                    name.toCharArray().forEach {
                        sb.append(Pinyin.toPinyin(it).toUpperCase(Locale.getDefault()))
                    }
                    field = sb.toString()
                }
                return field
            }
        var char: Char = ' '
            get() {
                if (field == ' ') {
                    field = letter[0]
                }
                return field
            }

        private val pattern = "yyyyMMdd HH:mm"

        fun time(): String {
            return "${pattern.formatDate(installTime)} / ${
                pattern.formatDate(lastUpdateTime)
            }\r\n$useTimeIn7 $size"
        }

        override fun compareTo(other: AppItem): Int {
            return letter.compareTo(other.letter)
        }

        override fun isHeaderItem(): Boolean = isHeader

        override fun headerStr(): String = char.toString()

        override fun toString(): String {
            return "AppItem(name='$name', icon=$icon, pkgName='$pkgName', size=$size, installTime=$installTime, lastUpdateTime=$lastUpdateTime, useTimeIn7=$useTimeIn7, isHeader=$isHeader, letter='$letter', char=$char)"
        }
    }
}