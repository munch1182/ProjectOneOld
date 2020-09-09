package com.munch.test.view

import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.ripple.RippleDrawableCompat
import com.munch.lib.libnative.helper.ResHelper
import com.munch.lib.libnative.helper.ViewHelper
import com.munch.lib.log.LogLog
import com.munch.test.R
import com.munch.test.base.BaseActivity
import com.munch.test.view.weight.BgTextView
import com.munch.test.view.weight.FlowLayout
import kotlinx.android.synthetic.main.activity_test_view2.*


/**
 * Create by Munch on 2020/09/04
 */
class TestView2Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_view2)
        setToolBar(view_tb, "View")


        val listener = View.OnClickListener {
            val gravity = when (it.tag.toString().toInt()) {
                0 -> FlowLayout.START
                1 -> FlowLayout.END
                2 -> FlowLayout.CENTER_HORIZONTAL
                3 -> FlowLayout.CENTER_VERTICAL
                4 -> FlowLayout.CENTER
                5 -> FlowLayout.END_CENTER_VERTICAL
                else -> FlowLayout.START
            }
            view_flow.setGravity(gravity)
        }
        view_b1.setOnClickListener(listener)
        view_b2.setOnClickListener(listener)
        view_b3.setOnClickListener(listener)
        view_b4.setOnClickListener(listener)
        view_b5.setOnClickListener(listener)
        view_b6.setOnClickListener(listener)

        var textView: TextView
        val dp5 = ResHelper.dp2Px(dpVal = 5f).toInt()

        /*view_flow2.setGravity(FlowLayout.CENTER)*/
        for (i in 0..30) {
            textView = BgTextView(this)
            textView.text = ((30..5000).random() * 0.3f).toString()
            textView.setPadding(dp5 * 2, dp5, dp5 * 2, dp5)
            ViewHelper.setViewMargin(textView, dp5, dp5 / 2, dp5, dp5 / 2)
            textView.isClickable = true
            textView.background =
                ResHelper.getSystemDrawable(resId = android.R.attr.selectableItemBackground)
            view_flow2.addView(textView)
        }

        view_btn_del.setOnClickListener {
            if (view_flow2.childCount == 0) {
                return@setOnClickListener
            }
            view_flow2.removeViewAt(0)
            if (view_flow2.childCount == 0) {
                (view_btn_del.parent as View).visibility = View.GONE
                return@setOnClickListener
            }
        }
    }
}