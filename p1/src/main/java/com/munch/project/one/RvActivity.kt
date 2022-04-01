package com.munch.project.one

import android.os.Bundle
import android.os.SystemClock
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.helper.ViewCreator
import com.munch.lib.android.recyclerview.*
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
        val adapter = AdapterHelper(object :
            BaseRecyclerViewAdapter<String, BaseViewHolder>({ c -> TextView(c).apply { setPadding(16) } }) {
            override fun onBind(holder: BaseViewHolder, position: Int, bean: String) {
                (holder.itemView as TextView).text = bean
            }
        })

        adapter.setOnItemClickListener { _, pos, _ ->
            Toast.makeText(this, "$pos", Toast.LENGTH_SHORT).show()
        }

        adapter.bind(rv)
        adapter.showEmpty()

        thread {
            SystemClock.sleep(1000L)
            adapter.hideEmpty()
            adapter.showRefresh()
            SystemClock.sleep(1000L)
            adapter.add(arrayListOf("1", "2", "3"))
            adapter.hideRefresh()
            adapter.showLoadMore()
            SystemClock.sleep(1000L)
            adapter.add("4")
            adapter.hideLoadMore()
        }
    }
}