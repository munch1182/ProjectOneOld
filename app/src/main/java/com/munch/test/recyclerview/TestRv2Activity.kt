package com.munch.test.recyclerview

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.munch.lib.libnative.helper.ViewHelper
import com.munch.test.R
import com.munch.test.base.RvActivity
import kotlinx.android.synthetic.main.activity_rv.*

/**
 * Create by Munch on 2020/09/09
 */
class TestRv2Activity : RvActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolBar(rv_tb)

    }

    override fun setRv(list: ArrayList<String>) {
        /*super.setRv(list)*/
        rv_rv.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter =
            object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_rv_main, list) {
                override fun convert(holder: BaseViewHolder, item: String) {
                    if (list.size > 1) {
                        if (holder.adapterPosition == 0 || holder.adapterPosition == list.size - 1) {
                            holder.itemView.layoutParams.height = 150
                            holder.itemView.requestLayout()
                        }
                    }
                    holder.setText(R.id.item_tv, item)
                }

                override fun onCreateDefViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): BaseViewHolder {
                    return BaseViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_rv_main, parent, false)
                            .apply {
                                setBackgroundColor(Color.GREEN)
                                ViewHelper.setViewMargin(this, 16, 16, 16, 16)
                                this.layoutParams.height = 300
                            }
                    )
                }

                override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
                    super.onItemViewHolderCreated(viewHolder, viewType)

                }
            }
        rv_rv.adapter = adapter
    }

    override fun addItemList(list: ArrayList<String>) {
        super.addItemList(list)
        for (i in 0..30) {
            list.add(((5..999).random() * 0.1f).toString())
        }
    }
}