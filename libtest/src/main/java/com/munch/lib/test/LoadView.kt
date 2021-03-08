package com.munch.lib.test

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar

/**
 * Create by munch1182 on 2021/2/23 17:44.
 */
class LoadView(context: Context) : FrameLayout(context) {

    private val progressBar by lazy { ProgressBar(context) }

    init {
        addView(progressBar, LayoutParams(160, 160).apply {
            gravity = Gravity.CENTER
        })
        setBackgroundColor(Color.parseColor("#80000000"))
    }

}

class LoadViewProxy : ILoading {

    private var loadView: LoadView? = null

    override fun init(context: Context) {
        loadView = LoadView(context)
    }

    override fun getView(): View {
        return loadView ?: throw IllegalStateException("must init first")
    }

    override fun startLoading() {
    }

    override fun stopLoading() {
    }
}