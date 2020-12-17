package com.munch.project.test.bar

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.munch.lib.helper.BarHelper
import com.munch.lib.helper.startActivity
import com.munch.lib.log
import com.munch.lib.test.R
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean

/**
 * Create by munch1182 on 2020/12/12 21:16.
 */
class TestBarActivity : TestRvActivity() {

    private val barHelper by lazy { BarHelper(this) }

    override fun setupIntent() {
        super.setupIntent()
        intent.putExtras(
            newBundle(
                "Bar",
                TestRvItemBean.newArray(
                    "hide",
                    "color",
                    "fullscreen",
                    "change text color",
                    "next activity"
                ),
                isBtn = true
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (topView.parent as View).setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.colorPrimaryDark
            )
        )
        log(window.statusBarColor)
    }

    private var hide = false
    private var full = false
    private var text = false
    private var color = true

    override fun clickItem(view: View, pos: Int) {
        super.clickItem(view, pos)
        when (pos) {
            0 -> {
                hide = !hide
                barHelper.hideStatusBar(hide)
            }
            1 -> {
                color = !color
                barHelper.colorStatusBar(
                    if (color) Color.TRANSPARENT else ContextCompat.getColor(
                        this,
                        R.color.colorPrimary
                    )
                )
            }
            2 -> {
                full = !full
                barHelper.fullScreen(full)
            }
            3 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                text = !text
                barHelper.setTextColorBlack(text)
            }
            4 -> nextActivity()
        }
    }

    private fun nextActivity() {
        startActivity(TestBar4FragmentActivity::class.java)
    }

}