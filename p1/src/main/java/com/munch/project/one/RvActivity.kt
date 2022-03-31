package com.munch.project.one

import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.recyclerview.EmptyAdapter
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2022/3/31 22:39.
 */
class RvActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rv = RecyclerView(this)
        setContentView(rv)


        rv.layoutManager = LinearLayoutManager(this)
        val adapter = EmptyAdapter("当前没有数据")

        rv.adapter = adapter
        adapter.show()

        thread {
            SystemClock.sleep(1000L)
            adapter.hide()
            SystemClock.sleep(1000L)
            adapter.show()
        }
    }
}