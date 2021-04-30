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
) : ToJson {
    override fun toJson(): String {
        return "{\"url\":\"$url\", \"name\":\"$name\"}"
    }
}

@Entity(tableName = AppDatabase.TB_LINK_TAG)
data class ItemTag(
    val tag: String,
    var count: Int = 1,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : ToJson {
    fun add(): ItemTag {
        return ItemTag(tag, count + 1, id)
    }

    override fun toJson(): String {
        return "{\"tag\":\"$tag\", \"count\":$count, \"id\":$id}"
    }
}

interface ToJson {

    fun toJson(): String
}

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

    @Query("SELECT * FROM ${AppDatabase.TB_LINK_TAG}")
    suspend fun queryAllTag(): MutableList<ItemTag>

    @Query("SELECT * FROM ${AppDatabase.TB_LINK} WHERE :tag IN (tag) ORDER BY priority")
    suspend fun queryByTag(tag: String): MutableList<ItemLink>

    @Query("SELECT * FROM ${AppDatabase.TB_LINK} ORDER BY addTime")
    suspend fun queryAllLink(): MutableList<ItemLink>

    @Query("SELECT * FROM ${AppDatabase.TB_LINK_TAG} WHERE tag == :tag")
    suspend fun getTagCount(tag: String): ItemTag?

    @Insert
    suspend fun addTag(type: ItemTag)

    @Update
    suspend fun updateTag(type: ItemTag)

    @Transaction
    suspend fun updateTag(tag: String) {
        val type = getTagCount(tag)
        if (type == null) {
            addTag(ItemTag(tag, 1))
        } else {
            updateTag(type.add())
        }
    }

    @Insert
    suspend fun addLink(link: ItemLink)

    @Transaction
    suspend fun addLinkAndUpdateTag(link: ItemLink) {
        addLink(link)
        link.tag.forEach { updateTag(it) }
    }
}

@Database(entities = [ItemLink::class, ItemTag::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val TB_LINK = "tb_link_item"
        const val TB_LINK_TAG = "tb_link_item_tag"
    }

    abstract fun linkDao(): LinkDao
}

object DbHelper {

    private val db by lazy {
        Room.databaseBuilder(BaseApp.getInstance(), AppDatabase::class.java, "db_link").build()
    }

    fun getLinkDao() = db.linkDao()
}