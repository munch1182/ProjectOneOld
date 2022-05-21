package cn.munch.lib.record

import androidx.room.*
import cn.munch.lib.RecordDB
import com.munch.lib.extend.toDate
import kotlinx.coroutines.flow.Flow

/**
 * Create by munch1182 on 2022/4/18 13:53.
 */
@Entity(tableName = RecordDB.NAME_TB)
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

    override fun toString(): String {
        return "${recordTime.toDate()}: $log"
    }
}

@Dao
interface RecordDao {

    @Query("SELECT * FROM ${RecordDB.NAME_TB} ORDER BY recordTime DESC")
    suspend fun queryAll(): List<Record>

    @Query("SELECT * FROM ${RecordDB.NAME_TB} ORDER BY recordTime DESC LIMIT :size OFFSET :page*:size")
    suspend fun queryAll(page: Int = 0, size: Int = 15): List<Record>

    //对于flow的返回，当数据有更改时，会自动触发数据的更新的分发
    //但是这种触发时任意更新都会触发
    //如果要指定触发，可以使用flow的特性.distinctUntilChanged()
    @Query("SELECT * FROM ${RecordDB.NAME_TB} ORDER BY recordTime DESC")
    fun queryAllFlow(): Flow<List<Record>>

    @Query("SELECT * FROM ${RecordDB.NAME_TB} WHERE id == :id")
    suspend fun query(id: Long): Record?

    //为null的值须为空
    @Query(
        "SELECT * FROM ${RecordDB.NAME_TB}" +
                " WHERE ((:type == -1) OR (:type == type))" +
                " AND ((:like == '') OR (log LIKE :like OR thread LIKE :like OR stack LIKE :like OR comment LIKE :like))" +
                " AND ((:time == 0) OR (recordTime >= :time)) ORDER BY recordTime DESC" +
                " LIMIT :size OFFSET :page*:size"
    )
    suspend fun query(
        type: Int,
        like: String,
        time: Long,
        page: Int = 0,
        size: Int = 15
    ): List<Record>

    @Query(
        "SELECT COUNT(*) FROM ${RecordDB.NAME_TB}" +
                " WHERE (:type == -1 OR :type == type)" +
                " AND (:like == '' OR (log LIKE :like OR thread LIKE :like OR stack LIKE :like OR comment LIKE :like)" +
                " AND (:time == 0 OR recordTime >= :time))" +
                " LIMIT :size OFFSET :page*:size"
    )
    suspend fun querySize(type: Int, like: String?, time: Long, page: Int = 0, size: Int = 15): Int

    @Query("SELECT COUNT(*) FROM ${RecordDB.NAME_TB}")
    fun querySizeFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ${RecordDB.NAME_TB}")
    suspend fun querySize(): Int

    @Insert
    suspend fun insert(vararg record: Record)

    suspend fun insert(log: String) = insert(Record(log))
    suspend fun time(log: String) = insert(Record(log, Record.TYPE_TIME_MEASURE))

    @Delete
    suspend fun del(vararg record: Record)

    @Query("DELETE FROM ${RecordDB.NAME_TB}")
    suspend fun clear()

    @Query("DELETE FROM ${RecordDB.NAME_TB} WHERE id == :id")
    suspend fun delById(id: Long)

    @Update
    suspend fun update(vararg record: Record)
}

