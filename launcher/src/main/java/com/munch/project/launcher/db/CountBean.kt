package com.munch.project.launcher.db

import androidx.room.*

/**
 * Create by munch1182 on 2021/2/24 11:56.
 */

/**
 * 当前月的统计数据，记录每次的数据，当月底时，归总数据到[CountAll]，然后清空并重建此数据
 */
@Entity(tableName = DbHelper.NAME_COUNT_NOW)
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

@Entity(tableName = DbHelper.NAME_COUNT_ALL)
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