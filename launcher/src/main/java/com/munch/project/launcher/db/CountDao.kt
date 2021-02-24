package com.munch.project.launcher.db

import androidx.room.*

/**
 * Create by munch1182 on 2021/2/24 11:50.
 */
@Dao
interface CountDao : BaseDao<CountNow> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg bean: CountAll)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(bean: CountAll)

    @Delete
    fun del(vararg bean: CountAll)


}
