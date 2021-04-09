package com.munch.test.project.one.recyclerview

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.pinyinhelper.PinyinDict
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.extend.formatDate
import com.munch.pre.lib.extend.log
import com.munch.pre.lib.helper.AppHelper
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Create by munch1182 on 2021/4/9 15:36.
 */
class AppItemViewModel : ViewModel() {

    private val appItems = flow {
        val pm = BaseApp.getInstance().packageManager ?: return@flow
        val queryUsageStats = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usage = BaseApp.getInstance()
                .getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager?
            if (usage != null) {
                val currentTimeMillis = System.currentTimeMillis()
                usage.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    currentTimeMillis - 24L * 60L * 60L * 1000L * 7, currentTimeMillis
                )
            } else {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
        val map = HashMap<String, Long>(queryUsageStats.size)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryUsageStats.forEach {
                map[it.packageName] = it.totalTimeForegroundServiceUsed
            }
        }

        emit(AppHelper.getInstallApp()?.map {
            val pkgName = it.activityInfo.packageName
            val info = pm.getPackageInfo(pkgName, PackageManager.GET_CONFIGURATIONS)
            AppItem(
                it.loadLabel(pm)?.toString() ?: "null",
                it.loadIcon(pm),
                pkgName,
                0,
                info.firstInstallTime,
                info.lastUpdateTime,
                map[pkgName] ?: 0L
            )
        }?.toMutableList() ?: mutableListOf())
    }

    val appItemsSortByLetter = appItems.map {
        it.sort()
        it
    }.asLiveData(viewModelScope.coroutineContext)


    data class AppItem(
        val name: String,
        val icon: Any?,
        val pkgName: String,
        val size: Long = 0L,
        val installTime: Long,
        val lastUpdateTime: Long,
        val useTimeIn7: Long
    ) : Comparable<AppItem> {

        var letter: String = " "
            get() {
                if (field.isEmpty()) {
                    val sb = StringBuilder()
                    name.toCharArray().forEach {
                        sb.append(Pinyin.toPinyin(it))
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

        fun time() = "${"yyyyMMdd HH:mm".formatDate(installTime)} / ${
            "yyyyMMdd HH:mm".formatDate(
                lastUpdateTime
            )
        }\r\n$useTimeIn7"

        override fun compareTo(other: AppItem): Int {
            return other.letter.compareTo(letter)
        }
    }
}