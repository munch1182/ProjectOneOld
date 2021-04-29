package com.munch.test.project.one.net.service

import androidx.room.*
import com.munch.pre.lib.base.BaseApp

/**
 * Create by munch1182 on 2021/4/29 17:24.
 */
@TypeConverters(TagConverts::class)
@Entity(tableName = AppDatabase.TB_LINK)
data class ItemLink(
    val url: String,
    val name: String,
    val tag: MutableList<String>,
    //优先级：默认0，一般123
    var priority: Int = 0,
    val addTime: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(tableName = AppDatabase.TB_LINK_TYPE)
data class ItemType(
    val tag: String,
    var count: Int = 1,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

class TagConverts {

    companion object {
        private const val SPLIT = ","
    }

    @TypeConverter
    fun tag2Str(tag: MutableList<String>): String {
        return tag.joinToString(separator = SPLIT)
    }

    @TypeConverter
    fun str2Tags(str: String): MutableList<String> {
        return str.split(SPLIT).toMutableList()
    }
}

@Dao
interface LinkDao {

    @Query("SELECT * FROM ${AppDatabase.TB_LINK_TYPE}")
    suspend fun queryAllType(): MutableList<ItemType>

    @Query("SELECT * FROM ${AppDatabase.TB_LINK} WHERE :tag IN (tag) ORDER BY priority")
    suspend fun queryByType(tag: String): MutableList<ItemLink>

    @Query("SELECT * FROM ${AppDatabase.TB_LINK} ORDER BY addTime")
    suspend fun queryAllLink(): MutableList<ItemLink>

    @Query("SELECT count FROM ${AppDatabase.TB_LINK_TYPE} WHERE tag == :tag")
    suspend fun getTagCount(tag: String): Int?

    @Insert
    suspend fun addTag(type: ItemType)

    @Transaction
    suspend fun addTag(tag: String) {
        val count = getTagCount(tag) ?: 1
        addTag(ItemType(tag, count))
    }

    @Insert
    suspend fun addLink(link: ItemLink)

    @Transaction
    suspend fun addLinkAndUpdateTag(link: ItemLink) {
        addLink(link)
        link.tag.forEach { addTag(it) }
    }
}

@Database(entities = [ItemLink::class, ItemType::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val TB_LINK = "tb_link_item"
        const val TB_LINK_TYPE = "tb_link_item_type"
    }

    abstract fun linkDao(): LinkDao
}

object DbHelper {

    private val db by lazy {
        Room.databaseBuilder(BaseApp.getInstance(), AppDatabase::class.java, "db_link").build()
    }

    fun getLinkDao() = db.linkDao()
}