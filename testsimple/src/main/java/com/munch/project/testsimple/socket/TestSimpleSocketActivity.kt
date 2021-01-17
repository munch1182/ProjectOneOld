package com.munch.project.testsimple.socket

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.helper.TimerHelper.Companion.withTimer
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.def.ProgressDialog
import com.munch.lib.test.recyclerview.TestRvAdapter
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.R
import java.util.*
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2020/12/23 3:20.
 */
class TestSimpleSocketActivity : TestBaseTopActivity() {

    private val helper by lazy { SocketHelper(owner = this) }
    private val btnIp by lazy { findViewById<Button>(R.id.socket_btn_ip) }
    private val tvIp by lazy { findViewById<TextView>(R.id.socket_tv_ip) }
    private val btnIpAll by lazy { findViewById<Button>(R.id.socket_btn_ip_all) }
    private val rv: RecyclerView by lazy { findViewById(R.id.socket_rv) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_simple_activity_test_simple_socket)
        btnIp.setOnClickListener {
            tvIp.text = helper.getIpAddress() ?: "获取ip失败"
        }
        val testRvAdapter = TestRvAdapter()
        rv.adapter = testRvAdapter
        rv.layoutManager = LinearLayoutManager(this)
        btnIpAll.setOnClickListener {
            scanIp(testRvAdapter)
        }
    }

    private fun scanIp(testRvAdapter: TestRvAdapter) {
        val dialog = ProgressDialog(this).withTimer(this)
        thread {
            val ipAddressInNet = helper.getIpAddress()
            if (ipAddressInNet == null) {
                toast("wifi不可用")
                return@thread
            }
            dialog.show()
            helper.scanIpInNet(ipAddressInNet, {
                it.sortWith { o1, o2 ->
                    fun last(str: String) = str.split(".")[3].toInt()
                    return@sortWith last(o1) - last(o2)
                }
                runOnUiThread {
                    testRvAdapter.setData(MutableList(it.size) { i ->
                        TestRvItemBean.newInstance(it[i])
                    })
                    dialog.cancel()
                }
            }, {
                it.printStackTrace()
                dialog.cancelNow()
                log(it.message)
                toast(it.message ?: "error")
            })
        }
    }

}