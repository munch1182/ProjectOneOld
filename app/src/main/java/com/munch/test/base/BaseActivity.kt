package com.munch.test.base

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.munch.lib.libnative.helper.BarHelper
import com.munch.lib.libnative.helper.SystemResHelper
import com.munch.lib.libnative.root.RootActivity
import com.munch.test.R

/**
 * Created by Munch on 2019/8/24 11:04
 */
abstract class BaseActivity : RootActivity() {

    override fun toast(message: String) {
        super.toast(message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    open fun setStatusBar() {
        BarHelper.with(this).setTransparent()
    }

    open fun setToolBar(bar: Toolbar, title: String? = getString(R.string.test_text)) {
        bar.navigationIcon = SystemResHelper.getBackIcon(this)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bar.navigationIcon!!.setTint(Color.WHITE)
        }
        bar.title = title
        bar.setTitleTextColor(Color.WHITE)
        bar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}