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
                DateHelper.dayMillis(currentTimeMillis),
                currentTimeMillis
            )
        }
    }
}

/**
 * 设计中，该表只有一个值，
 * 即到昨天为止的偏移量
 *
 * 清空偏移也不能进行删除，除非要重置
 */
@Entity(tableName = NAME_TB_STEP_OFFSET)
data class StepOffsetBean(
    @PrimaryKey val id: Int = 0,
    val offset: Int,
    val lastDay: Long
) {

    companion object {
        fun update(offset: Int): StepOffsetBean {
            return StepOffsetBean(
                offset = offset,
                lastDay = DateHelper.dayMillis(System.currentTimeMillis())
            )
        }

    }
}

@Dao
interface StepDao {

    /**
     * 查询是否有今日即以前的偏移量，有则返回，无则返回null
     * 因为设计中，已经计数的偏移量统一合并为一个数据
     */
    @Query("SELECT * FROM $NAME_TB_STEP_OFFSET LIMIT 1")
    suspend fun queryOffsetBean(): StepOffsetBean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOffset(offsetBean: StepOffsetBean)

    /**
     * 注意：删除偏移表会导致偏移操作重置
     */
    @Query("DELETE FROM $NAME_TB_STEP_OFFSET")
    suspend fun delStepOffset()


    @Transaction
    suspend fun queryAllStepNotToday(): Int? {
        return queryAllStepBeforeDay(todayMillis())
    }

    fun todayMillis() = DateHelper.dayMillis(System.currentTimeMillis())

    @Transaction
    suspend fun addStepNoOffset(stepWithOffsetFromSensor: Int): Pair<StepBean, Int> {
        val todayMillis = todayMillis()

        val stepOffsetBean = queryOffsetBean()

        var stepOffset: Int
        //用offsetBean是否有值来判断是否是第一次使用，因此不能轻易删除
        //第一次使用，将已有的值都当作偏移值
        if (stepOffsetBean == null) {
            insertOrUpdateOffset(StepOffsetBean.update(stepWithOffsetFromSensor))
            stepOffset = stepWithOffsetFromSensor
        } else {
            //判断是否需要更新偏移表
            //如果昨天的值没有加入偏移
            if (todayMillis - stepOffsetBean.lastDay > DateHelper.TIME_HOUR_MIN_SEC) {
                //加上昨天的偏移
                stepOffset = stepOffsetBean.offset + (queryAllStepBeforeDay(todayMillis) ?: 0)
                insertOrUpdateOffset(StepOffsetBean.update(stepOffset))
            } else {
                stepOffset = stepOffsetBean.offset
            }
        }
        //传感器的数据小于数据库的偏移量，则是手机重启，传感器重新获取的数据，则直接计算今天的数据，同时更新偏移
        //不用去考虑是否有新的数据生成
        //因为运行的前提是每天都能保存到运动数据
        val todayStep: Int
        if (stepWithOffsetFromSensor < stepOffset) {
            todayStep = stepWithOffsetFromSensor
            stepOffset = 0
            //清空偏移而不删除
            insertOrUpdateOffset(StepOffsetBean.update(0))
            //传感器数据大于或者等于数据库的偏移量，则计算偏移，获取当天的数据
        } else {
            //需要更新每天的偏移量
            //传感器今天的数据
            todayStep = stepWithOffsetFromSensor - stepOffset
        }
        val stepBean = queryStepBeanByDay(todayMillis)?.update(todayStep) ?: StepBean.now(todayStep)
        //使用insert替换掉旧的值
        insert(stepBean)
        return Pair(stepBean, stepOffset)
    }

    @Transaction
    suspend fun queryTodayStep(): Int? {
        return queryStepByDay(todayMillis())
    }

    @Query("SELECT SUM(`offset`) FROM $NAME_TB_STEP_OFFSET WHERE lastDay < :date")
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