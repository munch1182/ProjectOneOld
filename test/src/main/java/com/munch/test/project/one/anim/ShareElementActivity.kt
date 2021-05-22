package com.munch.test.project.one.anim

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity

/**
 * Create by munch1182 on 2021/4/13 10:43.
 */
open class ShareElementActivity : BaseTopActivity() {

    private var targetView: View? = null

    override fun setToolBar(toolbar: Toolbar?) {
        super.setToolBar(toolbar)
        toolbar ?: return
        toolbar.children.forEach {
            if (it is TextView) {
                ViewCompat.setTransitionName(it, getString(R.string.share_element_title))
                targetView = it
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.TRANSPARENT
    }
}