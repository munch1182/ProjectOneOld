package com.munch.lib.fast.view.dispatch

import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

/**
 * 将Activity的活动分发出去
 * 其余的生命周期可以在onCreate中使用Lifecycle获取
 *
 * 可用于Activity固定设置的分发
 *
 * 注意: [dispatchers]会一直持有对应对象
 */
interface ActivityDispatch {

    val dispatchers: MutableList<ActivityDispatch>?
        get() = null

    fun onCreateActivity(activity: AppCompatActivity) {
        dispatchers?.forEach { it.onCreateActivity(activity) }
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

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        getActivityDispatcher().onCreateActivity(this)
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        getActivityDispatcher().onCreateActivity(this)
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        getActivityDispatcher().onCreateActivity(this)
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()
        getActivityDispatcher().onDestroy(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}