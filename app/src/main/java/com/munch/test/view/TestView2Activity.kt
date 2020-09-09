package com.munch.test.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import com.munch.test.R
import com.munch.test.base.BaseActivity
import com.munch.test.view.weight.FlowLayout
import kotlinx.android.synthetic.main.activity_test_view2.*


/**
 * Create by Munch on 2020/09/04
 */
class TestView2Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_view2)
        setToolBar(view_tb, "View")


        val listener = View.OnClickListener {
            val gravity = when (it.tag.toString().toInt()) {
                0 -> FlowLayout.START
                1 -> FlowLayout.END
                2 -> FlowLayout.CENTER_HORIZONTAL
                3 -> FlowLayout.CENTER_VERTICAL
                4 -> FlowLayout.CENTER
                5 -> FlowLayout.END_CENTER_VERTICAL
                else -> FlowLayout.START
            }
            view_flow.setGravity(gravity)
        }
        view_b1.setOnClickListener(listener)
        view_b2.setOnClickListener(listener)
        view_b3.setOnClickListener(listener)
        view_b4.setOnClickListener(listener)
        view_b5.setOnClickListener(listener)
        view_b6.setOnClickListener(listener)

        val listener2 = View.OnClickListener {
            (view_chip.parent as ViewGroup).removeView(it)
        }
        view_chip.setOnCloseIconClickListener(listener2)
        view_chip2.setOnCloseIconClickListener(listener2)

        view_rv.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW).apply {
            flexWrap = FlexWrap.WRAP
        }
        val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(0),
            DraggableModule {

            override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return createBaseViewHolder(Chip(parent.context))
            }

            override fun convert(holder: BaseViewHolder, item: String) {
                if (holder.itemView is Chip) {
                    (holder.itemView as Chip).text = item
                }
            }
        }
        adapter.draggableModule.isDragEnabled = true
        view_rv.adapter = adapter

        val list = ArrayList<String>(49)
        for (i in 0..50) {
            list.add(((55..888).random() * 0.1f).toString())
        }
        adapter.setList(list)
    }

}