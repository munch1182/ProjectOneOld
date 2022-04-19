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

object DBRecord : RecordDao by RecordHelper.getInstance(AppHelper.app).record

@Database(entities = [Record::class], version = 1)
abstract class RecordDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao
}

class RecordHelper private constructor(app: Application) {

    companion object : SingletonHolder<RecordHelper, Application>({ RecordHelper(it) }) {
        internal const val NAME_TB = "tb_record"
    }

    private val db = Room.databaseBuilder(app, RecordDatabase::class.java, "db_record").build()

    val record: RecordDao = db.recordDao()
}