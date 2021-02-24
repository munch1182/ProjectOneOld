package com.munch.project.launcher.db

import androidx.room.*

/**
 * Create by munch1182 on 2021/2/24 11:29.
 */
@Dao
interface AppDao : BaseDao<AppBean> {

    @Query("SELECT * FROM ${DbHelper.NAME_TB_APP} ORDER BY app_id")
    suspend fun queryAll(): List<AppBean>?

    @Query("SELECT * FROM ${DbHelper.NAME_TB_APP} ORDER BY app_name")
    suspend fun queryAllByName(): List<AppBean>?
}