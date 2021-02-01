package com.munch.project.testsimple.net

import android.animation.ObjectAnimator
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.extend.recyclerview.BaseSimpleBindAdapter
import com.munch.lib.helper.setTextCompat
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.TestSimpleItemSocketTvBinding

/**
 * Create by munch1182 on 2021/1/29 15:16.
 */
class TestClipActivity : TestBaseTopActivity() {

    private val model by get(TestClipViewModel::class.java)
    private val btnStart: Button by lazy { findViewById(R.id.clip_btn_start) }
    private val btnStop: Button by lazy { findViewById(R.id.clip_btn_stop) }
    private val btnSend: Button by lazy { findViewById(R.id.clip_btn_send) }
    private val btnDisconnect: Button by lazy { findViewById(R.id.clip_btn_disconnect) }
    private val rv: RecyclerView by lazy { findViewById(R.id.clip_btn_rv) }
    private val et: EditText by lazy { findViewById(R.id.clip_et) }
    private val helper by lazy { StatusHelper(findViewById(R.id.clip_status)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_simple_activity_clip)
        btnStart.setOnClickListener { model.startSearch() }
        btnStop.setOnClickListener { model.close() }
        btnSend.setOnClickListener { model.sendText(et.text.toString()) }
        btnDisconnect.setOnClickListener { model.disconnect() }

        val adapter = BaseSimpleBindAdapter<SocketContentBean, TestSimpleItemSocketTvBinding>(
            R.layout.test_simple_item_socket_tv, null
        ) { holder, data, _ ->
            holder.binding.bean = data
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        adapter.setOnItemClick { _, _, data, _ ->
            model.copy2Clip(data.content)
            etShowClip()
        }
        model.getNetListData().observe(this) { adapter.setData(it) }
        /*et.nonInput()*/
        model.getStatus().observe(this) { helper.updateStatus(it) }
    }

    override fun onResume() {
        super.onResume()
        //适配android10获取剪切板内容
        rv.post { etShowClip() }
    }

    private fun etShowClip() {
        model.queryClip()?.let {
            et.setTextCompat(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    class StatusHelper(private val imageView: ImageView) {

        fun updateStatus(status: TestClipViewModel.Status) {
            val id = when {
                status.isScanning() -> R.drawable.test_simple_ic_scan
                status.isConnecting() -> R.drawable.test_simple_ic_connecting
                status.isConnected() -> R.drawable.test_simple_ic_connected
                status.isClosed() -> -1
                else -> return
            }
            val alpha = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f)
            alpha.duration = 300L
            alpha.addListener(onEnd = {
                imageView.alpha = 1f
                if (id != -1) {
                    imageView.setImageResource(id)
                } else {
                    imageView.setImageDrawable(null)
                }
            })
            alpha.start()
        }
    }

}