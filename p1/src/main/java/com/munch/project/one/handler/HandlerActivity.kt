package com.munch.project.one.handler

import android.os.*
import android.widget.Button
import com.munch.lib.base.ViewHelper
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.log.log
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/10/6 14:08.
 */
class HandlerActivity : BaseBigTextTitleActivity() {

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = Button(this)
        setContentView(view, ViewHelper.newWWLayoutParams())

        view.setOnClickListener {
            val nextInt = Random.nextInt(1000, 10000)
            notify(nextInt)
        }

        val ht = HandlerThread("TEST_HT")
        ht.start()
        handler = THandler(ht.looper)
    }

    private fun notify(nextInt: Int) {
        log(nextInt)
        handler?.sendEmptyMessage(nextInt)
    }
}

class THandler(looper: Looper) : Handler(looper) {
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        log("receive:${msg.what}")
        SystemClock.sleep(3000L)
    }
}