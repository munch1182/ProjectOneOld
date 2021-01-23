package com.munch.project.testsimple.net

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.recyclerview.SimpleExpandableAdapter
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R

/**
 * 使用udp发送广播，再根据广播的回应建立tcp连接通信
 * Create by munch1182 on 2021/1/22 16:32.
 */
class TestSocketBroadcastAndConnectActivity : TestBaseTopActivity() {

    private val model by lazy { ViewModelProvider(this).get(TestSocketBroadcastViewModel::class.java) }
    private var animator: ObjectAnimator? = null

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

        model.getClientData().observe(this) {
            adapter.setData(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    private fun bindAdapter(
        viewClickListener: View.OnClickListener,
        rv: RecyclerView
    ): SimpleExpandableAdapter<TestSocketBroadcastViewModel.SocketBean> {
        val simpleExpandableAdapter = SimpleExpandableAdapter(
            mutableListOf(
                R.layout.test_simple_item_socket_name,
                R.layout.test_simple_item_socket_op
            ), null
        ) { adapter: SimpleExpandableAdapter<TestSocketBroadcastViewModel.SocketBean>, holder, data, position ->
            if (data is TestSocketBroadcastViewModel.SocketBean.SocketClientBean) {
                holder.itemView.apply {
                    findViewById<TextView>(R.id.socket_tv_name_title).text = data.ip
                    setOnClickListener {
                        val iv = findViewById<ImageView>(R.id.socket_iv_arrow)
                        if (data.isExpand) {
                            adapter.reduce(data)
                            rotation(90f, 0f, iv)
                        } else {
                            adapter.expand(data)
                            rotation(0f, 90f, iv)
                        }
                        data.isExpand = !data.isExpand
                    }
                }
            } else if (data is TestSocketBroadcastViewModel.SocketBean.SocketClientInfoBean) {
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

        simpleExpandableAdapter.setData(
            mutableListOf(
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance()
            )
        )

        return simpleExpandableAdapter
    }

    /**
     * 简单的属性动画无需如此复杂
     */
    private fun rotation(from: Float, to: Float, view: View) {
        animator = ObjectAnimator.ofFloat(view, "rotation", from, to)
        animator?.addListener(onEnd = {
            animator = null
        })
        animator?.start()
    }

    override fun onStop() {
        super.onStop()
        animator?.cancel()
    }


}