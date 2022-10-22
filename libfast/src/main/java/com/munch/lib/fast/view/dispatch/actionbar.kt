package com.munch.lib.fast.view.dispatch

import androidx.appcompat.app.AppCompatActivity

/**
 * Created by munch1182 on 2022/4/17 4:09.
 */
interface ISupportActionBar : ActivityDispatch {

    val showHome: Boolean
        get() = true

    override fun onCreateActivity(activity: AppCompatActivity) {
        super.onCreateActivity(activity)
        if (showHome) activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.title = activity::class.simpleName?.replace("Activity", "")
    }
}

class SupportActionBar(override val showHome: Boolean = true) : ISupportActionBar {
    override val dispatchers: MutableList<ActivityDispatch> = mutableListOf()
}