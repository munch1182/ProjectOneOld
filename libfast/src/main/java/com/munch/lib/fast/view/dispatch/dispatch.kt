package com.munch.lib.fast.view.dispatch

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

/**
 * 将Activity的活动分发出去
 * 其余的生命周期可以在onCreate中使用Lifecycle获取
 *
 * 可用于Activity固定设置的分发
 */
interface ActivityDispatch {

    val dispatchers: MutableList<ActivityDispatch>?
        get() = null

    fun onCreateActivity(activity: AppCompatActivity) {
        dispatchers?.forEach { it.onCreateActivity(activity) }
    }

    fun onOptionsItemSelected(activity: AppCompatActivity, item: MenuItem): Boolean {
        dispatchers?.forEach {
            if (it.onOptionsItemSelected(activity, item)) {
                return true
            }
        }
        return false
    }

    fun onCreateOptionsMenu(activity: AppCompatActivity, menu: Menu): Boolean {
        dispatchers?.forEach {
            if (!it.onCreateOptionsMenu(activity, menu)) {
                return true
            }
        }
        return true
    }

    fun getActivityDispatcher() = this

    /**
     * 如果有单个实现，则可以用接口的默认实现实现本接口并由需要的类实现
     * 如果有多个实现，可以进行组合，即让需要的类实现本接口，并使用类委托委托给所有组合的实现类相加的结果
     * 注意：相加后被添加的实现是否实现、实现顺序由添加的实现对ActivityDispatch默认实现的调用决定
     */
    operator fun plus(dispatch: ActivityDispatch): ActivityDispatch {
        dispatchers?.add(dispatch)
        return this
    }

    fun onDestroy(activity: AppCompatActivity) {
        dispatchers?.forEach { it.onDestroy(activity) }
        dispatchers?.clear()
    }
}

open class DispatcherActivity : AppCompatActivity(), ActivityDispatch {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActivityDispatcher().onCreateActivity(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return getActivityDispatcher().onOptionsItemSelected(this, item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getActivityDispatcher().onCreateOptionsMenu(this, menu)
        return super<AppCompatActivity>.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()
        getActivityDispatcher().onDestroy(this)
    }
}