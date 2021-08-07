package com.munch.project.one.applib.recyclerview

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.UnComplete
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter
import com.munch.lib.recyclerview.BaseViewHolder
import com.munch.lib.recyclerview.SingleViewModule
import com.munch.lib.recyclerview.setContentView
import com.munch.project.one.applib.R
import com.munch.project.one.applib.databinding.ActivityRvBinding
import kotlin.random.Random

/**
 * 单类型RV
 *
 * @see com.munch.lib.recyclerview.BaseRecyclerViewAdapter
 * Create by munch1182 on 2021/8/6 16:54.
 */
@UnComplete
class RvSingleActivity : AppCompatActivity() {

    private val bind by lazy {
        DataBindingUtil.setContentView<ActivityRvBinding>(this, R.layout.activity_rv)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val adapter =
            object : BaseRecyclerViewAdapter<String, BaseViewHolder>(), SingleViewModule {
                init {
                    singleViewHelper.setContentView { TextView(it) }
                }

                override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
                    (holder.itemView as? TextView)?.text = data[position] ?: "null"
                }
            }
        bind.rvRv.apply {
            layoutManager = LinearLayoutManager(this@RvSingleActivity)
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