package com.munch.project.launcher.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.munch.project.launcher.base.App

/**
 * Create by munch1182 on 2021/2/24 11:27.
 */
class DbHelper private constructor() {

    companion object {

        private val INSTANCE by lazy { DbHelper() }

        fun getInstance() = INSTANCE

        const val NAME_TB_APP = "tb_app"

        const val NAME_DB = "db_launcher"

        const val NAME_COUNT_NOW = "tb_app_count_now"
        const val NAME_COUNT_ALL = "tb_app_count_all"
    }

    private val roomDb =
        Room.databaseBuilder(App.getInstance(), Db::class.java, NAME_DB).build()

    fun getDb() = roomDb

    @Database(entities = [AppBean::class, CountNow::class, CountAll::class], version = 1)
    abstract class Db : RoomDatabase() {

        abstract fun appDao(): AppDao

        abstract fun countDao(): CountDao
    }
}


