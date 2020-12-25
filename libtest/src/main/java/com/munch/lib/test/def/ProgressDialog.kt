package com.munch.lib.test.def

import android.content.Context
import android.os.Bundle
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
            addView(ProgressBar(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    context.dp2Px(80f).toInt(),
                    context.dp2Px(80f).toInt()
                )
            })
        })
    }
}