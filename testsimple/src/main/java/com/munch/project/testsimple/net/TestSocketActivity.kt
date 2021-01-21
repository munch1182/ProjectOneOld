package com.munch.project.testsimple.net

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.munch.lib.helper.LogLog
import com.munch.lib.helper.clickItem
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R

/**
 * Create by munch1182 on 2021/1/21 14:37.
 */
class TestSocketActivity : TestBaseTopActivity() {

    private val helper = SocketHelper()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_simple_activity_test_socket)
        val tv = findViewById<TextView>(R.id.socket_tv)
        LogLog.setListener(this) { _, msg ->
            runOnUiThread {
                tv.text = "${tv.text}\n$msg"
                (tv.parent as NestedScrollView).fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
        findViewById<ViewGroup>(R.id.socket_container).clickItem({
            val tag = it.tag as? Int? ?: return@clickItem
            when (tag) {
                0 -> {
                    helper.startSocketService()
                }
                1 -> {
                    helper.stopSocketService()
                }
                2 -> {
                    helper.connect()
                }
                3 -> {
                    helper.send(msg = "123\n234")
                }
                4 -> {
                    helper.disconnect()
                }
                5 -> {
                    tv.text = ""
                }
            }
        }, Button::class.java)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        helper.disconnect()
        helper.stopSocketService()
    }
}