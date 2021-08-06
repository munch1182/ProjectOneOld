package com.munch.project.one.applib.recyclerview

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter
import com.munch.lib.recyclerview.BaseViewHolder
import com.munch.lib.recyclerview.MultiViewModule
import com.munch.project.one.applib.R
import com.munch.project.one.applib.databinding.ActivityRvBinding
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/8/6 17:37.
 */
class RvMultiActivity : AppCompatActivity() {

    companion object {
        private const val TYPE = 0
        private const val TYPE1 = 1
    }

    private val bind by lazy {
        DataBindingUtil.setContentView<ActivityRvBinding>(this, R.layout.activity_rv)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val adapter = object : BaseRecyclerViewAdapter<String, BaseViewHolder>(), MultiViewModule {
            init {
                multiViewHelper
                    .setType {
                        if (it == 0 || it == 5) TYPE else TYPE1
                    }
                    .setTypeView(TYPE, R.layout.item_rv_simple_tv)
                    .setTypeView(TYPE1) {
                        TextView(it).apply { setTextColor(Color.MAGENTA) }
                    }
            }

            override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
                (holder.itemView as? TextView)?.text = data[position] ?: "null"
            }
        }
        bind.rvRv.apply {
            layoutManager = LinearLayoutManager(this@RvMultiActivity)
            this.adapter = adapter
        }
        adapter.add(
            mutableListOf(
                Random.nextInt().toString(),
                Random.nextInt().toString(),
                Random.nextInt().toString(),
                Random.nextInt().toString(),
                Random.nextInt().toString(),
                Random.nextInt().toString(),
                Random.nextInt().toString()
            )
        )
    }
}