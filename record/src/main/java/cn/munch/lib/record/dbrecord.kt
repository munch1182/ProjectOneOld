package cn.munch.lib.record

import androidx.room.*
import cn.munch.lib.RecordHelper
import kotlinx.coroutines.flow.Flow

/**
 * Create by munch1182 on 2022/4/18 13:53.
 */


@Entity(tableName = RecordHelper.NAME_TB)
data class Record(
    val log: String,
    val type: Int = TYPE_MSG,
    val thread: String = Thread.currentThread().name,
    val stack: String? = null,
    val comment: String? = null,
    val recordTime: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Long = 0
) {

    companion object {
        const val TYPE_MSG = 0
        const val TYPE_ERROR = 1
        const val TYPE_EXCEPTION = 2
        const val TYPE_OTHER = 3
        const val TYPE_TIME_MEASURE = 4
    }

    @Suppress("UNUSED_EXPRESSION")
    val typeStr: String
        get() {
            return when (type) {
                TYPE_MSG -> "msg"
                TYPE_ERROR -> "error"
                TYPE_EXCEPTION -> "exception"
                TYPE_OTHER -> "other"
                TYPE_TIME_MEASURE -> "measure"
                else -> type.toString()
            }
        }
}

@Dao
interface RecordDao {
    @Query("SELECT * FROM ${RecordHelper.NAME_TB} WHERE id = :id")
    suspend fun query(id: Long): Record?

    @Query("SELECT * FROM ${RecordHelper.NAME_TB}")
    suspend fun queryAll(): List<Record>

    @Query("SELECT COUNT(*) FROM ${RecordHelper.NAME_TB}")
    fun querySizeFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ${RecordHelper.NAME_TB}")
    suspend fun querySize(): Int

    @Query("SELECT COUNT(*) FROM ${RecordHelper.NAME_TB} WHERE type in (:type)")
    suspend fun querySizeBy(type: Int): Int

    //对于flow的返回，当数据有更改时，会自动触发数据的更新的分发
    //但是这种触发时任意更新都会触发
    //如果要指定触发，可以使用flow的特性.distinctUntilChanged()
    @Query("SELECT * FROM ${RecordHelper.NAME_TB}")
    fun queryAllFlow(): Flow<List<Record>>

    @Query("SELECT * FROM ${RecordHelper.NAME_TB} WHERE type IN (:type)")
    suspend fun queryByType(type: Int): List<Record>

    @Query("SELECT * FROM ${RecordHelper.NAME_TB} WHERE type IN (:type)")
    fun queryByTypeFlow(type: Int): Flow<List<Record>>

    @Query("SELECT * FROM ${RecordHelper.NAME_TB} WHERE recordTime > :time")
    suspend fun queryAfterTime(time: Long): List<Record>

    @Query("SELECT * FROM ${RecordHelper.NAME_TB} WHERE recordTime > :time")
    fun queryAfterTimeFlow(time: Long): Flow<List<Record>>

    @Query("SELECT * FROM ${RecordHelper.NAME_TB} WHERE log LIKE :like OR thread LIKE :like OR stack LIKE :like OR comment LIKE :like ")
    suspend fun queryLike(like: String): List<Record>

    @Query("SELECT * FROM ${RecordHelper.NAME_TB} WHERE log LIKE :like OR thread LIKE :like OR stack LIKE :like OR comment LIKE :like ")
    fun queryLikeFlow(like: String): Flow<List<Record>>

    @Query("SELECT type FROM ${RecordHelper.NAME_TB}")
    suspend fun queryType(): List<Int>

    @Insert
    suspend fun insert(vararg record: Record)

    suspend fun insert(log: String) = insert(Record(log))
    suspend fun time(log: String) = insert(Record(log, Record.TYPE_TIME_MEASURE))

    @Delete
    suspend fun del(record: Record)

    @Query("DELETE FROM ${RecordHelper.NAME_TB} WHERE id == :id")
    suspend fun delById(id: Long)

    @Update
    suspend fun update(record: Record)
}

