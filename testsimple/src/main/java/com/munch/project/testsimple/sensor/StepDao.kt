package com.munch.project.testsimple.sensor

import android.content.Context
import androidx.room.*
import com.munch.lib.BaseApp
import com.munch.lib.helper.DateHelper
import com.munch.project.testsimple.sensor.DbStep.NAME_TB_STEP
import com.munch.project.testsimple.sensor.DbStep.NAME_TB_STEP_OFFSET

/**
 * Create by munch1182 on 2021/1/20 13:45.
 */
@Entity(tableName = NAME_TB_STEP)
data class StepBean(
    val step: Int,
    /**
     * 用long型date来标记日期，可以以此为范围来分年月日
     */
    @PrimaryKey val date: Long,
    val saveTime: Long = System.currentTimeMillis()
) : Comparable<StepBean> {

    override fun compareTo(other: StepBean): Int {
        return date.compareTo(other.date)
    }

    fun update(step: Int): StepBean {
        return StepBean(step, date, System.currentTimeMillis())
    }

    companion object {

        fun now(step: Int): StepBean {
            val currentTimeMillis = System.currentTimeMillis()
            return StepBean(
                step,
                DateHelper.dayMillis(currentTimeMillis - DateHelper.TIME_EIGHT_HOUR),
                currentTimeMillis
            )
        }
    }
}

@Entity(tableName = NAME_TB_STEP_OFFSET)
data class StepOffsetBean(
    val offset: Int,
    val from: Long = 0,
    @PrimaryKey val to: Long
) {
    fun update(offset: Int): StepOffsetBean {
        return StepOffsetBean(offset, from, to)
    }

    companion object {
        fun first(offset: Int): StepOffsetBean {
            return StepOffsetBean(
                offset = offset,
                from = 0L,
                to = DateHelper.dayMillis(System.currentTimeMillis() - DateHelper.TIME_EIGHT_HOUR)
            )
        }
    }
}

@Dao
interface StepDao {

    @Query("SELECT * FROM $NAME_TB_STEP_OFFSET  WHERE `to` <= :date ORDER BY `to` LIMIT 1")
    suspend fun queryOffsetBefore(date: Long): StepOffsetBean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(offsetBean: StepOffsetBean)

    @Transaction
    suspend fun queryOffsetBeforeToday(): StepOffsetBean? {
        return queryOffsetBefore(todayMillis())
    }

    @Update(entity = StepOffsetBean::class)
    suspend fun updateOffset(bean: StepOffsetBean)

    @Transaction
    suspend fun queryTodayStep(): Int? {
        return queryStepByDay(todayMillis())
    }

    @Transaction
    suspend fun queryTodayStepBean(): StepBean? {
        return queryStepBeanByDay(todayMillis())
    }

    @Transaction
    suspend fun queryAllStepNotToday(): Int? {
        return queryAllStepBeforeDay(todayMillis())
    }

    fun todayMillis() =
        DateHelper.dayMillis(System.currentTimeMillis() - DateHelper.TIME_EIGHT_HOUR)

    @Transaction
    suspend fun addStepNoOffset(stepWithOffset: Int): Pair<StepBean, Int> {
        val todayMillis = todayMillis()

        var offsetBean = queryOffsetBefore(todayMillis)
        //用offsetBean是否有值来判断是否是第一次使用，因此不能轻易删除
        //第一次使用，将已有的值都当作偏移值
        if (offsetBean == null) {
            offsetBean = StepOffsetBean.first(stepWithOffset)
            insert(offsetBean)
        }
        var stepOffset = offsetBean.offset
        //传感器的数据小于数据库的偏移量，则是手机重启，传感器重新获取的数据，则直接计算今天的数据，同时更新偏移
        //不用去考虑是否有新的数据生成
        //因为运行的前提是每天都能保存到运动数据
        val todayStep: Int
        if (stepWithOffset < stepOffset) {
            todayStep = stepWithOffset
            //清空偏移而不是删除
            stepOffset = 0
            updateOffset(offsetBean.update(stepOffset))
            //传感器数据大于或者等于数据库的偏移量，则计算偏移，获取当天的数据
        } else {
            //传感器今天的数据
            todayStep = stepWithOffset - stepOffset
        }
        val stepBean = queryStepBeanByDay(todayMillis)?.update(todayStep) ?: StepBean.now(todayStep)
        insert(stepBean)
        return Pair(stepBean, stepOffset)
    }

    @Query("SELECT SUM(step) FROM $NAME_TB_STEP WHERE date < :date")
    suspend fun queryAllStepBeforeDay(date: Long): Int?

    @Query("SELECT step FROM $NAME_TB_STEP WHERE date = :date")
    suspend fun queryStepByDay(date: Long): Int?

    @Query("SELECT * FROM $NAME_TB_STEP WHERE date = :date")
    suspend fun queryStepBeanByDay(date: Long): StepBean?

    @Query("SELECT step FROM $NAME_TB_STEP  WHERE date > :from AND date < :to ORDER BY date LIMIT :size OFFSET :page ")
    suspend fun queryStep(from: Long, to: Long, page: Int = 0, size: Int = 100): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stepBean: StepBean)

    @Update(entity = StepBean::class)
    suspend fun updateStep(vararg stepBean: StepBean)

    /* @Query("UPDATE $NAME_TB_STEP SET step = :step AND saveTime = :saveTime WHERE date = :date ")
     suspend fun updateStep(date: Long, step: Int, saveTime: Long = System.currentTimeMillis()):Int*/

    @Query("DELETE FROM $NAME_TB_STEP")
    suspend fun clear()

}

@Database(entities = [StepBean::class, StepOffsetBean::class], version = 1)
abstract class AppStepDatabase : RoomDatabase() {

    abstract fun stepDao(): StepDao
}

object DbStep {

    const val NAME_TB_STEP = "tb_step"
    const val NAME_TB_STEP_OFFSET = "tb_step_offset"

    fun getDb(context: Context = BaseApp.getContext()): AppStepDatabase {
        return Room.databaseBuilder(context, AppStepDatabase::class.java, "db_step.db")
            .build()
    }

    fun stepDao(context: Context = BaseApp.getContext()) = getDb(context).stepDao()
}