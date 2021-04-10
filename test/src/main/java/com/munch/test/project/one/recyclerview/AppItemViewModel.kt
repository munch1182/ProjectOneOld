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
        sortByLetter().map {
            val map = hashMapOf<Char, Int>()
            var char = ' '
            var index = 0
            it.forEach { app ->
                if (char != app.char) {
                    map[char] = index
                    char = app.char
                }
                index++
            }
            Pair(it, map)
        }.asLiveData(viewModelScope.coroutineContext + Dispatchers.Default)
    }

    private fun sortByLetter() = appItems.map {
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
    }

    private var appSpan = 4

    val appItemSortByGroup by lazy {
        sortByLetter().map { list ->
            val array = mutableListOf<AppGroupItem>()
            var last = ' '
            var charIndex = -1
            val map = hashMapOf<Char, Int>()
            list.forEach {
                charIndex++
                //如果该数据的char与上一个char不同，则需要另起一组
                if (it.char != last && charIndex != 0) {
                    //给每一组最后一个位置添加一个占据剩余整行的空数据来占位置，需要GridLayoutManager.SpanSizeLookup配合
                    //此处计算上一个位置需要添加的空行数
                    val i = appSpan - (charIndex - 1) % appSpan - 1
                    //如果该组最后一个位置也是该行最后一个则不添加
                    if (i != 0) {
                        array.add(AppGroupItem.fromEmpty(last, charIndex, i))
                    }
                    charIndex = 0
                }
                last = it.char.toUpperCase()
                array.add(AppGroupItem.fromIndex(it, charIndex))
                if (charIndex == 0) {
                    map[it.char] = array.size - 1
                }
            }
            Pair(array, map)
        }.asLiveData(viewModelScope.coroutineContext + Dispatchers.Default)
    }

    fun span(span: Int): AppItemViewModel {
        appSpan = span
        return this
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

    data class AppGroupItem(
        var char: Char,
        var isEmpty: Boolean = false,
        //在该分组下的序列
        var indexInLetter: Int = -1,
        //当前位置距离该行末尾的位置，如果不在最后一个，则为1
        //其具体值取决于GridLayout的列数，更新时计算
        var span2End: Int = 1,
        val appItem: AppItem? = null
    ) : Comparable<AppGroupItem> {

        companion object {


            fun fromIndex(appItem: AppItem, indexInLetter: Int) =
                AppGroupItem(appItem.char, false, indexInLetter, 1, appItem)

            fun fromEmpty(char: Char, indexInLetter: Int, span2End: Int) =
                AppGroupItem(char, true, indexInLetter, span2End, null)
        }

        override fun compareTo(other: AppGroupItem): Int {
            val compareTo = char.compareTo(other.char)
            if (compareTo != 0) {
                return compareTo
            }
            appItem ?: return 1
            other.appItem ?: return -1
            return appItem.compareTo(other.appItem)
        }
    }
}