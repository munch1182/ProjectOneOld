package com.munch.project.launcher.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * Create by munch1182 on 2021/2/22 17:35.
 */
@Entity(tableName = "tb_app")
@TypeConverters(AppTypeConverters::class)
data class AppBean(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "app_id") val appId: Int = 0,
    //应用名称
    @ColumnInfo(name = "app_name")
    val name: String,
    //应用图标，所有资源必须转为string保持
    @ColumnInfo(name = "app_icon")
    val icon: String? = null,
    //包名
    val pkgName: String,
    @ColumnInfo(name = "app_launcher_activity")
    var launcherActivity: String? = null,
    //应用设置
    val set: AppSet
) {
    companion object {

        fun new(
            name: String,
            icon: String?,
            launcherActivity: String?,
            pkgName: String,
            index: Int
        ): AppBean {
            return AppBean(
                name = name,
                icon = icon,
                launcherActivity = launcherActivity,
                pkgName = pkgName,
                set = AppSet.def(index)
            )
        }
    }
}

class AppTypeConverters {

    @TypeConverter
    fun set2Str(set: AppSet?): String? {
        set ?: return null
        return set.convert2String()
    }

    @TypeConverter
    fun set2Info(str: String?): AppSet? {
        str ?: return null
        return AppSet.resumeFromString(str)
    }
}


/**
 * 设置相关
 */
data class AppSet(
    //应用被排序的位置
    var index: Int,
    //是否置顶
    var top: Boolean = false,
    //是否隐藏显示
    var hide: Boolean = false,
    //用户设置的分组
    var group: List<String> = arrayListOf(),
    //应用添加的标记，启动器将根据标记对其自动操作
    @Hide var flag: List<String> = arrayListOf()
) : StringConverter {

    companion object {

        fun resumeFromString(string: String): AppSet? {
            val split = StringConverter.splitStr(string)
            if (split.size != 5) {
                return null
            }
            return AppSet(
                split[0].toInt(),
                split[1].toBoolean(),
                split[2].toBoolean(),
                StringConverter.splitStr(split[3]),
                StringConverter.splitStr(split[4])
            )
        }

        fun def(index: Int) = AppSet(index)
    }

    fun convert2String(): String {
        return convert2String(
            arrayListOf(
                index.toString(),
                top.toString(),
                hide.toString(),
                StringConverter.convertList2String(group),
                StringConverter.convertList2String(flag)
            )
        )
    }
}

/**
 * 当前月的统计数据，记录每次的数据，当月底时，归总数据到[CountAll]，然后清空并重建此数据
 */
@Entity(tableName = "tb_app_count_now")
data class CountNow(
    @ColumnInfo(name = "app_id")
    val appId: Int,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "app_count_id")
    val id: Int,
    //开始使用的时间
    @ColumnInfo(name = "app_count_start")
    val start: Long,
    //结束使用的时间
    @ColumnInfo(name = "app_count_end")
    var end: Long = 0L
)

@Entity(tableName = "tb_app_count_all")
@TypeConverters(UseTimeConverts::class)
data class CountAll(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "app_count_id")
    val id: Int,
    @ColumnInfo(name = "app_id")
    val appId: Int,
    //当前记录的时间，以月为单位
    @ColumnInfo(name = "app_count_month")
    val month: Long,
    //当前月该应用的使用记录
    @ColumnInfo(name = "app_count_month_count")
    val monthCount: List<UseTime>
)

data class UseTime(
    //开始时间，时分秒的毫秒值
    val start: Long,
    //结束时间，时分秒的毫秒值，如无法记录仍然保留且为0
    var end: Long,
    //当天的年月日的毫秒值，真实的开始时间即为"${day+start}"，主要用来减少start和end的位数
    val day: Long
) : StringConverter {

    companion object {

        fun resumeFromString(string: String): UseTime? {
            val split = StringConverter.splitStr(string)
            if (split.size != 3) {
                return null
            }
            return UseTime(split[0].toLong(), split[1].toLong(), split[2].toLong())
        }
    }

    fun convert2String(): String {
        return convert2String(arrayListOf(start.toString(), end.toString(), day.toString()))
    }
}

class UseTimeConverts {

    @TypeConverter
    fun useTime2String(useTime: UseTime?): String? = useTime?.convert2String()

    @TypeConverter
    fun string2UseTime(string: String?): UseTime? {
        return UseTime.resumeFromString(string ?: return null)
    }
}
