package com.munch.lib.fast.view

import android.app.Activity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.UnImplException
import com.munch.lib.UnSupportException

/**
 * Created by munch1182 on 2022/4/16 1:03.
 */
/**
 * 将Activity的活动分发出去
 *
 * 可用于Activity固定设置的分发
 */
interface ActivityDispatch {

    val list: MutableList<ActivityDispatch>
        get() = mutableListOf()

    fun onCreateActivity(activity: AppCompatActivity) {
        list.forEach { it.onCreateActivity(activity) }
    }

    fun onOptionsItemSelected(activity: AppCompatActivity, item: MenuItem): Boolean {
        list.forEach {
            if (it.onOptionsItemSelected(activity, item)) {
                return true
            }
        }
        return false
    }

    fun getOnActivityCreate() = this

    operator fun plus(dispatch: ActivityDispatch): ActivityDispatch {
        list.add(dispatch)
        return this
    }

    fun onDestroy(activity: AppCompatActivity) {
        list.forEach { it.onDestroy(activity) }
        list.clear()
    }
}
