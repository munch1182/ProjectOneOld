package com.munch.lib.fast.view

import android.app.Activity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by munch1182 on 2022/4/16 1:03.
 */
/**
 * 将Activity的活动分发出去
 *
 * 可用于Activity固定设置的分发
 */
interface ActivityDispatch {

    fun onCreateActivity(activity: AppCompatActivity) {}

    fun onOptionsItemSelected(activity: Activity, item: MenuItem): Boolean {
        return false
    }

    fun getOnActivityCreate() = this
}

interface SupportActionBar : ActivityDispatch {

    val showHome: Boolean
        get() = true

    override fun onCreateActivity(activity: AppCompatActivity) {
        activity.supportActionBar?.apply {
            this.setDisplayHomeAsUpEnabled(showHome)
        }
        activity.title = activity::class.simpleName?.replace("Activity", "")
    }

    override fun onOptionsItemSelected(activity: Activity, item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            activity.onBackPressed()
            return true
        }
        return false
    }
}