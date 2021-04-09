package com.munch.test.project.one.recyclerview

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.databinding.ActivityBaseRvBinding
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.extend.observeOnChanged
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ItemHeaderRvBinding

/**
 * Create by munch1182 on 2021/4/9 15:29.
 */
class HeaderRvActivity : BaseTopActivity() {

    private val bind by bind<ActivityBaseRvBinding>(R.layout.activity_base_rv)
    private val model by get(AppItemViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        val appAdapter = object :
            BaseBindAdapter<AppItemViewModel.AppItem, ItemHeaderRvBinding>(R.layout.item_header_rv) {
            override fun onBindViewHolder(
                holder: BaseBindViewHolder<ItemHeaderRvBinding>,
                bean: AppItemViewModel.AppItem,
                pos: Int
            ) {
                holder.bind.app = bean
            }

        }
        bind.baseTopRv.apply {
            layoutManager = LinearLayoutManager(this@HeaderRvActivity)
            adapter = appAdapter
        }
        bind.baseTopSrl.setOnRefreshListener {
            bind.baseTopSrl.postDelayed({ bind.baseTopSrl.isRefreshing = false }, 600L)
        }
        model.appItemsSortByLetter.observeOnChanged(this) {
            appAdapter.set(it)
        }
    }
}