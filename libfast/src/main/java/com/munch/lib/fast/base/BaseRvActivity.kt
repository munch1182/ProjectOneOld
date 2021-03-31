package com.munch.lib.fast.base

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.fast.R
import com.munch.lib.fast.databinding.ActivityBaseRvBinding
import com.munch.lib.fast.databinding.ItemBaseTopBinding
import com.munch.lib.fast.helper.RvHelper
import com.munch.pre.lib.extend.startActivity

/**
 * Create by munch1182 on 2021/3/31 15:35.
 */
abstract class BaseRvActivity : BaseTopActivity() {

    private val bind by bind<ActivityBaseRvBinding>(R.layout.activity_base_rv)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.baseTopRv.apply {
            layoutManager = LinearLayoutManager(this@BaseRvActivity)
            addItemDecoration(RvHelper.newLineDecoration())
            adapter =
                object : BaseBindAdapter<ItemBean, ItemBaseTopBinding>(
                    R.layout.item_base_top, getItem()
                ) {
                    init {
                        setOnItemClickListener { _, bean, _, _ ->
                            if (bean.target != null) {
                                startActivity(bean.target)
                            }
                        }
                    }

                    override fun onBindViewHolder(
                        holder: BaseBindViewHolder<ItemBaseTopBinding>,
                        bean: ItemBean,
                        pos: Int
                    ) {
                        holder.bind.itemBaseTopTv.text = bean.name
                    }

                }
        }
    }

    abstract fun getItem(): MutableList<ItemBean>

    data class ItemBean(val name: String, val target: Class<out Activity>? = null) {

        companion object {

            fun newItem(target: Class<out Activity>): ItemBean {
                return ItemBean(target.simpleName.replace("Activity", ""), target)
            }

            fun newItems(vararg target: Class<out Activity>): MutableList<ItemBean> {
                return target.map { newItem(it) }.toMutableList()
            }
        }
    }
}