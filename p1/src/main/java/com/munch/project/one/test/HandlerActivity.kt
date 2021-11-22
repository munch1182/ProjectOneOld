package com.munch.project.one.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import com.munch.lib.base.ViewHelper
import com.munch.lib.base.setMarginOrKeep
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.log.log
import com.munch.lib.task.ThreadHandler
import com.munch.project.one.R
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/10/6 14:08.
 */
class HandlerActivity : BaseBigTextTitleActivity() {

    private val handler by lazy {
        ThreadHandler("TEST_HT") {
            log("receive:${it.what}")
            SystemClock.sleep(3000L)
            true
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = Button(this)
        setContentView(view, ViewHelper.newWWLayoutParams())

        view.setMarginOrKeep(l = resources.getDimensionPixelSize(R.dimen.paddingDef))
        view.text = "send"
        view.setOnClickListener {
            val nextInt = Random.nextInt(1000, 10000)
            notify(nextInt)
        }
    }

    private fun notify(nextInt: Int) {
        handler.post {
            log(nextInt)
            SystemClock.sleep(3000L)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.quit()
    }
}