package com.munch.lib.test.def

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.munch.lib.helper.dp2Px

/**
 * Create by munch1182 on 2020/12/25 17:37.
 */
class ProgressDialog(context: Context) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)

            addView(ProgressBar(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    context.dp2Px(50f).toInt(),
                    context.dp2Px(50f).toInt()
                ).apply {
                    gravity = Gravity.CENTER
                }
            })

            setCanceledOnTouchOutside(false)
        })
        window?.setBackgroundDrawable(null)
        setOnKeyListener { _, _, _ ->
            if (context is Activity) {
                (context as Activity).onBackPressed()
            }
            true
        }
    }
}