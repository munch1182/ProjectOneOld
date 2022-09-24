package com.munch.lib.fast.view.record

import androidx.room.*
import com.munch.lib.android.AppHelper

/**
 * Create by munch1182 on 2022/9/24 9:42.
 */
@Entity(tableName = RecordDB.NAME_TB)
data class Record(
    val tag: String,
    val content: String,
    val recordTime: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Dao
interface RecordDao {

    // 添加一个record
    @Insert
    suspend fun log(record: Record)

    // 分页查询所有的record
    @Query("SELECT * FROM ${RecordDB.NAME_TB} ORDER BY recordTime ASC LIMIT :size offset (:size * :page)")
    suspend fun query(page: Int, size: Int = 30): List<Record>

    // 分页查询指定tag下的所有record
    @Query("SELECT * FROM ${RecordDB.NAME_TB} WHERE tag = :tag ORDER BY recordTime ASC LIMIT :size offset (:size * :page)")
    suspend fun query(tag: String, page: Int, size: Int = 30): List<Record>

    // 删除所有数据
    @Query("DELETE FROM ${RecordDB.NAME_TB}")
    suspend fun del()

    // 删除指定id的数据
    @Query("DELETE FROM ${RecordDB.NAME_TB}  WHERE id = :id")
    suspend fun del(id: Long)

    // 删除指定tag下的所有数据
    @Query("DELETE FROM ${RecordDB.NAME_TB}  WHERE tag = :tag")
    suspend fun del(tag: String)
}

@Database(entities = [Record::class], version = 1)
abstract class RecordDB : RoomDatabase() {
    abstract fun dao(): RecordDao

    companion object {
        const val NAME_TB = "tb_record"
        private const val NAME_DB = "db_record"

        val instance =
            Room.databaseBuilder(AppHelper, RecordDB::class.java, NAME_DB)
                .build()
                .dao()
    }
}

object RecordHelper : RecordDao by RecordDB.instance {

    suspend fun log(tag: String, content: String) = log(Record(tag, content))
}