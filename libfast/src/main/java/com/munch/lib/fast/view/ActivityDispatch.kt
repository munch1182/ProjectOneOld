package com.munch.lib.fast.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by munch1182 on 2022/4/16 1:03.
 */
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

    fun getOnActivityCreate() = this

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
        getOnActivityCreate().onCreateActivity(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return getOnActivityCreate().onOptionsItemSelected(this, item)
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()
        getOnActivityCreate().onDestroy(this)
    }
}