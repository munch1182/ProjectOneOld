package com.munch.project.testsimple.socket

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.recyclerview.TestRvAdapter
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.R
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2020/12/23 3:20.
 */
class TestSimpleSocketActivity : TestBaseTopActivity() {

    private val helper by lazy { SocketHelper() }
    private val btnIp by lazy { findViewById<Button>(R.id.socket_btn_ip) }
    private val tvIp by lazy { findViewById<TextView>(R.id.socket_tv_ip) }
    private val btnIpAll by lazy { findViewById<Button>(R.id.socket_btn_ip_all) }
    private val rv: RecyclerView by lazy { findViewById(R.id.socket_rv) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_simple_socket)
        btnIp.setOnClickListener {
            tvIp.text = helper.getIpAddressInNet() ?: "获取ip失败"
        }
        val testRvAdapter = TestRvAdapter()
        rv.adapter = testRvAdapter
        rv.layoutManager = LinearLayoutManager(this)
        btnIpAll.setOnClickListener {
            thread {
                helper.scanIpInNet(helper.getIpAddressInNet()!!) {
                    runOnUiThread {
                        testRvAdapter.setData(MutableList(it.size) { i ->
                            TestRvItemBean.newInstance(
                                it[i]
                            )
                        })
                    }
                }
            }
        }
    }

}