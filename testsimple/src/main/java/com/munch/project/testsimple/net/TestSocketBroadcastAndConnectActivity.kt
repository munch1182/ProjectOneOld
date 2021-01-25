package com.munch.project.testsimple.net

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListenerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
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

    private var adapter: SimpleExpandableAdapter<TestSocketBroadcastViewModel.SocketBean>? = null
    private val model by lazy { ViewModelProvider(this).get(TestSocketBroadcastViewModel::class.java) }
    private var animator: ObjectAnimator? = null

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

        adapter?.setData(
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
        )

        model.getClientData().observe(this) {
            adapter?.setData(it)
        }
    }

    private fun newItemAnimator() = object : DefaultItemAnimator() {
        override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
            holder ?: return false
            val itemView = holder.itemView
            val animate = ViewCompat.animate(itemView)
            animate.setDuration(addDuration)
                .translationY(itemView.height.toFloat())
                .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                    override fun onAnimationEnd(view: View?) {
                        super.onAnimationEnd(view)
                        animate.setListener(null)
                        view?.translationY = 0f
                        dispatchAnimationsFinished()
                    }
                })
                .start()
            return true
        }

        override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
            holder ?: return false
            val itemView = holder.itemView
            val animate = ViewCompat.animate(itemView)
            animate.setDuration(removeDuration)
                .translationY(0f)
                .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                    override fun onAnimationEnd(view: View?) {
                        super.onAnimationEnd(view)
                        animate.setListener(null)
                        view?.translationY = view?.height?.toFloat() ?: 0f
                        dispatchAnimationsFinished()
                    }
                })
                .start()
            return true
        }
    }

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
                adapter?.reduce(pos)
                rotation(90f, 0f, iv)
            } else {
                adapter?.expand(pos)
                rotation(0f, 90f, iv)
            }
            data.isExpand = !data.isExpand
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