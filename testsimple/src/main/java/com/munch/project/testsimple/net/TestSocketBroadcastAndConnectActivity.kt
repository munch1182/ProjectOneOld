package com.munch.project.testsimple.net

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.recyclerview.DefaultItemAnimator
import com.munch.lib.extend.recyclerview.SimpleExpandableAdapter
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R

/**
 * 两个同一协议的未知设备的局域网连接
 *
 * Create by munch1182 on 2021/1/22 16:32.
 */
class TestSocketBroadcastAndConnectActivity : TestBaseTopActivity() {

    private var adapter: SimpleExpandableAdapter<TestSocketBroadcastViewModel.SocketBean>? = null
    private val model by lazy { ViewModelProvider(this).get(TestSocketBroadcastViewModel::class.java) }
    private var animator: ObjectAnimator? = null
    private val btnStart: Button by lazy { findViewById(R.id.socket_btn_start) }
    private val btnStop: Button by lazy { findViewById(R.id.socket_btn_stop) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_simple_activity_test_socket_broadcast)
        val rv = findViewById<RecyclerView>(R.id.socket_rv)

        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rv.itemAnimator = newItemAnimator()

        val viewClickListener = View.OnClickListener {
            val data = it.tag as? TestSocketBroadcastViewModel.SocketBean.SocketClientInfoBean?
                ?: return@OnClickListener
            when (it.id) {
                R.id.socket_btn_quit -> {
                    model.quit(data)
                }
                R.id.socket_btn_send -> {
                    model.send(data)
                }
            }
        }
        adapter = newAdapter(viewClickListener)
        rv.adapter = adapter

        /*adapter?.setData(
            mutableListOf(
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance(),
                TestSocketBroadcastViewModel.SocketBean.SocketClientBean.newInstance()
            )
        )*/

        model.getClientData().observe(this) { adapter?.setData(it) }
        btnStart.setOnClickListener { model.startSearch() }
        btnStop.setOnClickListener { model.close() }
    }

    private fun newItemAnimator() = DefaultItemAnimator()

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    private fun newAdapter(
        viewClickListener: View.OnClickListener
    ): SimpleExpandableAdapter<TestSocketBroadcastViewModel.SocketBean> {
        val itemClickListener = View.OnClickListener {
            val data = it.tag as? TestSocketBroadcastViewModel.SocketBean.SocketClientBean?
                ?: return@OnClickListener
            val iv = it.findViewById<ImageView>(R.id.socket_iv_arrow)
            val pos = adapter?.getData()?.indexOf(data) ?: return@OnClickListener
            if (data.isExpand) {
                data.isExpand = false
                adapter?.reduce(pos)
                rotation(90f, 0f, iv)
            } else {
                data.isExpand = true
                adapter?.expand(pos)
                rotation(0f, 90f, iv)
            }
        }
        return SimpleExpandableAdapter(
            mutableListOf(
                R.layout.test_simple_item_socket_name,
                R.layout.test_simple_item_socket_op
            ), null
        ) { _, holder, data, _ ->
            holder.itemView.tag = data
            if (data is TestSocketBroadcastViewModel.SocketBean.SocketClientBean) {
                holder.itemView.apply {
                    findViewById<TextView>(R.id.socket_tv_name_title).text = data.ip
                    setOnClickListener(itemClickListener)
                }
            } else if (data is TestSocketBroadcastViewModel.SocketBean.SocketClientInfoBean) {
                holder.itemView.apply {
                    findViewById<TextView>(R.id.socket_tv_name).text = data.ip
                    findViewById<TextView>(R.id.socket_tv_msg).text = data.msg
                    findViewById<View>(R.id.socket_btn_quit).setOnClickListener(viewClickListener)
                    findViewById<View>(R.id.socket_btn_send).setOnClickListener(viewClickListener)
                }
            }
        }
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