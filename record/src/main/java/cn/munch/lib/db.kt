package cn.munch.lib

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cn.munch.lib.record.Record
import cn.munch.lib.record.RecordDao
import com.munch.lib.AppHelper
import com.munch.lib.extend.SingletonHolder

/**
 * Created by munch1182 on 2022/4/19 20:17.
 */

object DBRecord : RecordDao by RecordDB.getInstance(AppHelper.app).record

@Database(entities = [Record::class], version = RecordDB.VERSION_22_03_20, exportSchema = true)
abstract class RecordDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao
}

class RecordDB private constructor(app: Application) {

    companion object : SingletonHolder<RecordDB, Application>({ RecordDB(it) }) {
        internal const val NAME_TB = "tb_record"
        internal const val NAME_DB = "db_record"
        internal const val NAME_DB_READ = "db_record_read"

        internal const val VERSION_22_03_20 = 1
    }

    private val db = Room.databaseBuilder(app, RecordDatabase::class.java, NAME_DB)
        /*.setQueryCallback(
            { sqlQuery, bindArgs -> log(sqlQuery, bindArgs) },
            ThreadHelper.newCachePool()
        )*/
        .build()

    val record: RecordDao = db.recordDao()
}