package com.munch.test.base

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.munch.test.R
import kotlinx.android.synthetic.main.activity_rv.*

/**
 * Create by munch on 2020/9/2 9:41
 */
open class RvActivity : BaseActivity() {

    lateinit var adapter: BaseQuickAdapter<String, BaseViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rv)

        setSupportActionBar(rv_tb)
        supportActionBar!!.elevation = 15f

        val itemList = ArrayList<String>().apply {
            addItemList(this)
        }
        setRv(itemList)
    }

    open fun setRv(list: ArrayList<String>) {
        rv_rv.layoutManager = LinearLayoutManager(this)

        adapter =
            object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_rv_main, list),
                DraggableModule {
                override fun convert(holder: BaseViewHolder, item: String) {
                    holder.setText(R.id.item_tv, item)
                }
            }
        rv_rv.adapter = adapter
    }

    open fun addItemList(list: ArrayList<String>) {
    }
}