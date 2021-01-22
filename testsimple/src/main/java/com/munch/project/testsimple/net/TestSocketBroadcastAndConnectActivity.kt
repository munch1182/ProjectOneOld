package com.munch.project.testsimple.net

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.recyclerview.ExpandableLevelData
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R

/**
 * 使用udp发送广播，再根据广播的回应建立tcp连接通信
 * Create by munch1182 on 2021/1/22 16:32.
 */
class TestSocketBroadcastAndConnectActivity : TestBaseTopActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_simple_activity_test_socket_broadcast)
        val rv = findViewById<RecyclerView>(R.id.socket_rv)
        /*rv.adapter = SimpleExpandableAdapter<SocketBean>()*/
    }

    sealed class SocketBean : ExpandableLevelData {


        data class SocketClientBean(
            val name: String,
            val ip: String,
            var isExpand: Boolean = false
        ) : SocketBean()

        data class SocketClientInfoBean(val ip: String, var msg: String) : SocketBean() {
            override fun expandLevel(): Int {
                return 1
            }
        }
    }

}