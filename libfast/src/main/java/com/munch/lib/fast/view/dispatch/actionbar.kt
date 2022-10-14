package com.munch.lib.fast.view.dispatch

import android.view.Menu
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
        if (showHome) activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.title = activity::class.simpleName?.replace("Activity", "")
    }

    override fun onOptionsItemSelected(activity: AppCompatActivity, item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            activity.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(activity, item)
    }
}

class SupportActionBar(override val showHome: Boolean = true) : ISupportActionBar {
    override val dispatchers: MutableList<ActivityDispatch> = mutableListOf()
}

interface ISupportShareActionBar : ISupportActionBar {

    companion object {

        private const val TITLE = "share"

        fun isShare(item: MenuItem) = item.title == TITLE
    }

    override fun onCreateOptionsMenu(activity: AppCompatActivity, menu: Menu): Boolean {
        menu.add(TITLE)
        return super.onCreateOptionsMenu(activity, menu)
    }
}

object SupportShareActionBar : ISupportShareActionBar {
    override val dispatchers: MutableList<ActivityDispatch> = mutableListOf()
}