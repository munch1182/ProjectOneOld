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
    private val udpHelper by lazy { SocketUdpHelper() }
    private val tvHelper: TextView by lazy { findViewById(R.id.socket_tv_switch) }
    private var type = 0

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
        tvHelper.text = getHelperStr()
        findViewById<ViewGroup>(R.id.socket_container).clickItem({
            val tag = it.tag as? Int? ?: return@clickItem
            when (tag) {
                0 -> {
                    getHelper().closeResource()
                    changeType()
                    tvHelper.text = getHelperStr()
                }
                1 -> {
                    getHelper().startSocketService()
                }
                2 -> {
                    getHelper().stopSocketService()
                }
                3 -> {
                    getHelper().clientConnect()
                }
                4 -> {
                    getHelper().clientSend(msg = "123\n234\nabandon")
                    if (getHelper() is SocketUdpHelper) {
                        (getHelper() as SocketUdpHelper).sendNetBroadcast()
                    }
                }
                5 -> {
                    getHelper().clientDisconnect()
                }
                6 -> {
                    tv.text = ""
                }
            }
        }, Button::class.java)
    }

    //<editor-fold desc="type方法">
    private fun changeType() {
        type++
        if (type > 1) {
            type = 0
        }
    }

    private fun getHelper(): ISocketHelper {
        return when (type) {
            0 -> helper
            1 -> udpHelper
            else -> helper
        }
    }

    private fun getHelperStr(): String {
        return when (type) {
            0 -> "socket"
            1 -> "udp"
            else -> "socket"
        }
    }
    //</editor-fold>

    override fun onBackPressed() {
        super.onBackPressed()
        getHelper().closeResource()
    }
}