# 定时任务
* 执行周期性、长时间、甚至是app生命周期之外的任务

## 方案1：Handler

* 优点：
    - 准时
    - 循环间隔可以小于15分钟
* 确定：必须依赖App环境

```kotlin
class WorkRunnable : Runnable {
    override fun run() {
        ```

        handler.postDelayed(this, 15*60*1000L)
    }

}
handler.postDelayed(thiWorkRunnable(), 15*60*1000L)

```

## 方案2：Alarm

- 优点：
    - 执行的是系统服务，如果手机运行自启动，则可以唤醒app
- 缺点：
    - 间隔至少为15分钟
    - 任务执行不会准时
```kotlin
val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager?
am?.setRepeating(AlarmManager.RTC_WEAKUP, System.currentTimeMillis() + 1000L,
                 AlarmManager.INTERVAL_FIFTEEN_MINUTES, PendingIntent)
```

## 方案3：WorkManager

* 优点：
    - 详情可查询

```kotlin
class WorkImp(context:Context,params:WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        ...
        return Result.success()
    }
}
val request = PeriodicWorkRequestBuilder<WorkImp>(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,TimeUnit.MILLISECONDS)
              .addTag("tag")
              .build()
WorkManager.getInstance(context).enqueueUniquePeriodicWork("name",ExistingPeriodicWorkPolicy.REPLACE,request)
```
