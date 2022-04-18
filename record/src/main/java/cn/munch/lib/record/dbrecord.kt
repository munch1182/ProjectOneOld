package cn.munch.lib.record

import android.app.Application
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Create by munch1182 on 2022/4/18 13:53.
 */
class RecordHelper(app: Application) {

    companion object {
        internal const val NAME_TB = "tb_record"

        @Volatile
        private var instance: RecordHelper? = null

        fun getInstance(app: Application): RecordHelper {
            val i = instance
            if (i != null) {
                return i
            }
            return synchronized(this) {
                val i2 = instance
                if (i2 != null) {
                    i2
                } else {
                    val created = RecordHelper(app)
                    instance = created
                    created
                }
            }
        }
    }


    val db = Room.databaseBuilder(app, RecordDatabase::class.java, "db_record").build()
}

@Entity(tableName = RecordHelper.NAME_TB)
data class Record(
    val log: String,
    val type: Int,
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
    }
}

@Dao
interface RecordDao {
    @Query("SELECT * FROM ${RecordHelper.NAME_TB} WHERE id = :id")
    suspend fun query(id: Long): Record?

    @Query("SELECT * FROM ${RecordHelper.NAME_TB}")
    suspend fun queryAll(): List<Record>

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

    @Delete
    fun del(record: Record)

    @Update
    fun update(record: Record)
}

@Database(entities = [Record::class], version = 1)
abstract class RecordDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao
}