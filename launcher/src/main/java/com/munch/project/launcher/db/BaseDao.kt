package com.munch.project.launcher.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * Create by munch1182 on 2021/2/24 11:51.
 */
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg bean: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(bean: T)

    @Delete
    fun del(vararg bean: T)
}