package com.munch.lib.fast.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.AppHelper
import com.munch.lib.extend.getColorPrimary
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.helper.BarHelper

/**
 * Created by munch1182 on 2022/4/15 23:04.
 */
open class BaseFastActivity : AppCompatActivity(), ActivityDispatch {

    protected val contentView: FrameLayout by lazy { findViewById(android.R.id.content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBar()
        getOnActivityCreate().onCreateActivity(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return getOnActivityCreate().onOptionsItemSelected(this, item)
    }

    fun toast(str: CharSequence) {
        runOnUiThread { Toast.makeText(AppHelper.app, str, Toast.LENGTH_SHORT).show() }
    }

    protected open fun onBar() {
        BarHelper(this).colorStatusBar(getColorPrimary())
    }
}