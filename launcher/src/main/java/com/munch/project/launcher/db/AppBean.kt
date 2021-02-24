package com.munch.project.launcher.db

import androidx.room.*
import com.munch.project.launcher.base.Hide

/**
 * Create by munch1182 on 2021/2/22 17:35.
 */
@Entity(tableName = DbHelper.NAME_TB_APP)
@TypeConverters(AppTypeConverters::class)
data class AppBean(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "app_id") val appId: Int = 0,
    //应用名称
    @ColumnInfo(name = "app_name")
    val name: String,
    //包名，其余信息都可以通过包名进行查找
    @ColumnInfo(name = "app_package_name")
    val pkgName: String,
    @ColumnInfo(name = "app_launcher_activity")
    var launcherActivity: String? = null,
    //应用设置
    val set: AppSet
) {
    companion object {

        fun new(
            name: String,
            launcherActivity: String?,
            pkgName: String,
            index: Int,
            icon: Any? = null
        ): AppBean {
            return AppBean(
                name = name,
                launcherActivity = launcherActivity,
                pkgName = pkgName,
                set = AppSet.def(index)
            ).apply { this.icon = icon }
        }
    }

    @Ignore
    var icon: Any? = null

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

