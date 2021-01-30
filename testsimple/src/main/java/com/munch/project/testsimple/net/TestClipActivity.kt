package com.munch.project.testsimple.net

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
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

    private val model by lazy { ViewModelProvider(this).get(TestClipViewModel::class.java) }
    private val btnStart: Button by lazy { findViewById(R.id.clip_btn_start) }
    private val btnStop: Button by lazy { findViewById(R.id.clip_btn_stop) }
    private val btnSend: Button by lazy { findViewById(R.id.clip_btn_send) }
    private val rv: RecyclerView by lazy { findViewById(R.id.clip_btn_rv) }
    private val et: EditText by lazy { findViewById(R.id.clip_et) }
    private val helper by lazy { StatusHelper(findViewById<ImageView>(R.id.clip_status)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_simple_activity_clip)
        btnStart.setOnClickListener {
            model.startSearch()
        }
        btnStop.setOnClickListener {
            model.close()
        }
        btnSend.setOnClickListener {
            model.sendText(et.text.toString())
        }

        val adapter = BaseSimpleBindAdapter<SocketContentBean, TestSimpleItemSocketTvBinding>(
            R.layout.test_simple_item_socket_tv, null
        ) { holder, data, _ ->
            holder.binding.bean = data
        }
        rv.adapter = adapter
        adapter.setOnItemClick { _, _, data, _ ->
            model.copy2Clip(data.content)
        }
        model.getClipListData().observe(this) { adapter.setData(it) }
        /*et.nonInput()*/
        model.getClipData().observe(this) { et.setTextCompat(it) }
        model.getStatus().observe(this) { helper.updateStatus(it) }
    }


    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    class StatusHelper(private val imageView: ImageView) {

        fun updateStatus(status: TestClipViewModel.Status) {
            imageView.visibility = if (status.isClosed()) View.GONE else View.VISIBLE
            val id = when {
                status.isScanning() -> R.drawable.test_simple_ic_scan
                status.isConnecting() -> R.drawable.test_simple_ic_connecting
                status.isConnected() -> R.drawable.test_simple_ic_connected
                status.isClosed() -> return
                else -> return
            }
            imageView.setImageResource(id)
        }
    }

}