package com.munch.project.testsimple.sensor

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.munch.lib.helper.DateHelper
import com.munch.lib.helper.ResultHelper
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.TestSimpleActivityTestSensorBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Create by munch1182 on 2021/1/20 9:53.
 */
@SuppressLint("SetTextI18n")
class TestSensorActivity : TestBaseTopActivity() {

    private val binding by bindingTop<TestSimpleActivityTestSensorBinding>(R.layout.test_simple_activity_test_sensor)
    private val listener by lazy {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val step = event?.values?.get(0)?.toInt() ?: 0
                //只在第一次显示的时候计算差异
                if (stepOffset == -1) {
                    lifecycleScope.launch {
                        judgeStepFormDbAndSenor(step)
                    }
                } else {
                    lastStep = step - stepOffset
                    showNowStep()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
    }

    private fun showNowStep() {
        if (lastStep == -1) {
            return
        }
        binding.testSensorTv.text = "今天步数: $lastStep"
    }

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var lastStep = -1
    private val stepDao by lazy { DbStep.stepDao() }
    private var stepOffset = -1
    private var inTime = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        binding.testSensorTv.text = "今天步数: null"

        requestSensorCompat()

        lifecycleScope.launch {
            //数据库未获取则赋值为-1不显示
            val queryTodayStep = stepDao.queryTodayStep() ?: -1
            //数据库先查到今天的值，而传感器还未注册
            lastStep = queryTodayStep
            showNowStep()
            val queryStepYesterday =
                stepDao.queryStepByDay(
                    DateHelper.dayMillis(System.currentTimeMillis() - DateHelper.TIME_HOUR_MIN_SEC)
                ) ?: return@launch
            binding.testSensorTvYesterday.text = "昨天：$queryStepYesterday"
        }

        binding.testSensorBtnWork.setOnClickListener {
            setTimerStart()
            toast("已注册")
        }
        binding.testSensorBtnWorkCancel.setOnClickListener {
            cancelTimerStart()
            toast("已取消")
        }
        binding.testSensorBtnStart.setOnClickListener {
            if (inTime) {
                return@setOnClickListener
            }
            inTime = true
            register()
            toast("已开始更新")
        }

    }

    private fun cancelTimerStart() {
        /*WorkManager.getInstance(this)
            .cancelAllWork()*/
        val alarmManager: AlarmManager =
            getSystemService(Context.ALARM_SERVICE) as? AlarmManager? ?: return
        val pendingIntent = getPendingIntent()
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun setTimerStart() {
        /*WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "step_sensor_work_unique",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<StepWork>(
                    12L,
                    TimeUnit.HOURS,
                    15,
                    TimeUnit.MINUTES
                ).build()
            )*/
        val alarmManager: AlarmManager? = getSystemService(Context.ALARM_SERVICE) as? AlarmManager?
        val pendingIntent = getPendingIntent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                DateHelper.newDate(hour = 23, min = 20, sec = 0).time,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } else {
            toast("版本低于android23")
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        return PendingIntent.getService(
            this,
            11,
            Intent("com.munch.project.step").setPackage(packageName),
            PendingIntent.FLAG_NO_CREATE
        )
    }

    private suspend fun judgeStepFormDbAndSenor(sensorStep: Int) {
        val stepBean = stepDao.addStepNoOffset(sensorStep)
        lastStep = stepBean.first.step
        stepOffset = stepBean.second
        showNowStep()
    }

    private fun requestSensorCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ResultHelper.with(this)
                .requestPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                .res { allGrant, _, _ ->
                    if (!allGrant) {
                        binding.testSensorTv.text = "权限被拒绝"
                    } else {
                        requestSensor()
                    }
                }
        } else {
            requestSensor()
        }
    }

    private fun requestSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager?
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            binding.testSensorTv.text = "设备不支持"
        }
    }

    override fun onResume() {
        super.onResume()
        if (inTime) {
            register()
        }
    }

    private fun register() {
        sensorManager?.registerListener(
            listener,
            stepSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateDb()
    }

    private fun updateDb() {
        if (lastStep == -1) {
            return
        }
        lifecycleScope.launch {
            stepDao.addStepNoOffset(lastStep)
        }
    }

    class QueryStepService : Service() {
        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onCreate() {
            super.onCreate()
            val sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager?
            val stepSensor =
                sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepSensor == null) {
                stopSelf()
            }
            sensorManager?.registerListener(
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        val step = event?.values?.get(0) ?: return
                        runBlocking {
                            DbStep.stepDao(this@QueryStepService).addStepNoOffset(step.toInt())
                        }
                        sensorManager.unregisterListener(this)
                        stopSelf()
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    }
                }, stepSensor, SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    class StepWork(private val appContext: Context, parameters: WorkerParameters) :
        Worker(appContext, parameters) {

        override fun doWork(): Result {
            //在其余work的时候就应该判断是否可用，因此此处不必判断是否需要停止
            val sensorManager = appContext.getSystemService(SENSOR_SERVICE) as? SensorManager?
            val stepSensor =
                sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return Result.success()
            sensorManager.registerListener(
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        val step = event?.values?.get(0) ?: return
                        runBlocking {
                            DbStep.stepDao(appContext).addStepNoOffset(step.toInt())
                        }
                        sensorManager.unregisterListener(this)
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    }
                }, stepSensor, SensorManager.SENSOR_DELAY_FASTEST
            )
            return Result.success()
        }
    }
}