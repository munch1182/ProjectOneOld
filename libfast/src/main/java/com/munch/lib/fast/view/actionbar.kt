package com.munch.lib.fast.view

import android.app.Activity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by munch1182 on 2022/4/17 4:09.
 */
interface ISupportActionBar : ActivityDispatch {

    val showHome: Boolean
        get() = true

    override fun onCreateActivity(activity: AppCompatActivity) {
        super.onCreateActivity(activity)
        activity.supportActionBar?.apply {
            this.setDisplayHomeAsUpEnabled(showHome)
        }
        activity.title = activity::class.simpleName?.replace("Activity", "")
    }

    override fun onOptionsItemSelected(activity: AppCompatActivity, item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            activity.onBackPressed()
            return true
        }
        return false
    }
}

object SupportActionBar : ISupportActionBar {
    override val list: MutableList<ActivityDispatch> = mutableListOf()
}