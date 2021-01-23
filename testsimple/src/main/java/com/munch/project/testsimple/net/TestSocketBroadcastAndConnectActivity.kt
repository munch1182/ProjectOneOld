package com.munch.project.testsimple.net

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.recyclerview.ExpandableLevelData
import com.munch.lib.extend.recyclerview.SimpleExpandableAdapter
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R

/**
 * 使用udp发送广播，再根据广播的回应建立tcp连接通信
 * Create by munch1182 on 2021/1/22 16:32.
 */
class TestSocketBroadcastAndConnectActivity : TestBaseTopActivity() {

    private val model by lazy { ViewModelProvider(this).get(TestSocketBroadcastAndConnectViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_simple_activity_test_socket_broadcast)
        val rv = findViewById<RecyclerView>(R.id.socket_rv)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val viewClickListener = View.OnClickListener {
            when (it.id) {
                R.id.socket_btn_quit -> {
                }
                R.id.socket_btn_send -> {
                }
            }
        }
        val adapter = bindAdapter(viewClickListener, rv)
    }

    private fun bindAdapter(
        viewClickListener: View.OnClickListener,
        rv: RecyclerView
    ): SimpleExpandableAdapter<SocketBean> {
        val simpleExpandableAdapter = SimpleExpandableAdapter(
            mutableListOf(
                R.layout.test_simple_item_socket_name,
                R.layout.test_simple_item_socket_op
            ), null
        ) { adapter: SimpleExpandableAdapter<SocketBean>, holder, data, position ->
            if (data is SocketBean.SocketClientBean) {
                holder.itemView.apply {
                    findViewById<TextView>(R.id.socket_tv_name_title).text = data.ip
                    setOnClickListener {
                        if (data.isExpand) {
                            adapter.reduce(data)
                            findViewById<ImageView>(R.id.socket_iv_arrow).setImageResource(R.drawable.test_simple_ic_arrow_forward)
                        } else {
                            adapter.expand(data)
                            findViewById<ImageView>(R.id.socket_iv_arrow).setImageResource(R.drawable.test_simple_ic_arrow_down)
                        }
                        data.isExpand = !data.isExpand
                    }
                }
            } else if (data is SocketBean.SocketClientInfoBean) {
                holder.itemView.apply {
                    findViewById<TextView>(R.id.socket_tv_name).text = data.ip
                    findViewById<TextView>(R.id.socket_tv_msg).text = data.msg
                    findViewById<View>(R.id.socket_btn_quit).apply {
                        tag = position
                    }.setOnClickListener(viewClickListener)
                    findViewById<View>(R.id.socket_btn_send).apply {
                        tag = position
                    }.setOnClickListener(viewClickListener)
                }
            }
        }
        rv.adapter = simpleExpandableAdapter

        /*simpleExpandableAdapter.setData(
            mutableListOf(
                SocketBean.SocketClientBean.newInstance(),
                SocketBean.SocketClientBean.newInstance(),
                SocketBean.SocketClientBean.newInstance(),
                SocketBean.SocketClientBean.newInstance()
            )
        )*/

        return simpleExpandableAdapter
    }

    sealed class SocketBean : ExpandableLevelData {

        data class SocketClientBean(
            val ip: String,
            val list: MutableList<ExpandableLevelData>? = null,
            var isExpand: Boolean = false
        ) : SocketBean() {

            override fun expandLevel(): Int {
                return 0
            }

            override fun getExpandableData(): MutableList<ExpandableLevelData>? {
                return list
            }

            companion object {

                fun newInstance(): SocketClientBean {
                    return SocketClientBean(
                        "192.168.0.1",
                        mutableListOf(
                            SocketClientInfoBean.newInstance()
                        )
                    )
                }
            }
        }

        data class SocketClientInfoBean(val ip: String, var msg: String) : SocketBean() {
            override fun expandLevel(): Int {
                return 1
            }

            companion object {

                fun newInstance(): SocketClientInfoBean {
                    return SocketClientInfoBean("192.168.0.1", "收到信息：aaaaaa\nbbbbb\nccccc\nddddd")
                }
            }
        }
    }

}