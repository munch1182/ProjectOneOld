package com.munch.test.project.one.recyclerview

import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.base.dialog.SimpleDialog
import com.munch.lib.fast.databinding.ActivityBaseRvBinding
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.base.rv.DiffItemCallback
import com.munch.pre.lib.extend.observeOnChanged
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.IntentHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ItemGroupRvBinding

/**
 * Create by munch1182 on 2021/4/9 15:32.
 */
class GroupRvActivity : BaseTopActivity() {

    private val bind by bind<ActivityBaseRvBinding>(R.layout.activity_base_rv)
    private val model by get(AppItemViewModel::class.java)
    private lateinit var appAdapter: BaseBindAdapter<AppItemViewModel.AppGroupItem, ItemGroupRvBinding>
    private val spanCount = 4

    private fun querySize() {
        model.span(spanCount).appItemSortByGroup.observeOnChanged(this) { data ->
            appAdapter.set(data.first)
            hideSrl()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        appAdapter = object :
            BaseBindAdapter<AppItemViewModel.AppGroupItem, ItemGroupRvBinding>(R.layout.item_group_rv) {
            init {
                diffUtil = object : DiffItemCallback<AppItemViewModel.AppGroupItem>() {

                    override fun areContentsTheSame(
                        oldItem: AppItemViewModel.AppGroupItem,
                        newItem: AppItemViewModel.AppGroupItem
                    ): Boolean {
                        val old = oldItem.appItem ?: return false
                        val new = newItem.appItem ?: return false
                        return old.name == new.name && old.pkgName == new.pkgName
                    }
                }
            }

            override fun onBindViewHolder(
                holder: BaseBindViewHolder<ItemGroupRvBinding>,
                bean: AppItemViewModel.AppGroupItem,
                pos: Int
            ) {
                holder.bind.app = bean
            }
        }
        bind.baseTopRv.apply {
            layoutManager = GridLayoutManager(this@GroupRvActivity, spanCount).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return appAdapter.get(position)!!.span2End
                    }
                }
            }
            setBackgroundColor(Color.WHITE)
            adapter = appAdapter
        }
        bind.baseTopSrl.apply { setOnRefreshListener { postDelayed({ hideSrl() }, 600L) } }
        showSrl()
        querySize()
    }

    private fun showSrl() {
        bind.baseTopSrl.isRefreshing = true
    }

    private fun hideSrl() {
        //错开更新的时间
        bind.baseTopSrl.apply {
            postDelayed({ isRefreshing = false }, 500L)
        }
    }
}