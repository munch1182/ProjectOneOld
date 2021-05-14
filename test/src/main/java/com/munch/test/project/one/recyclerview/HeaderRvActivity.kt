package com.munch.test.project.one.recyclerview

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.base.dialog.SimpleDialog
import com.munch.lib.fast.databinding.ActivityBaseRvBinding
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.extend.observeOnChanged
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.IntentHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ItemHeaderRvBinding
import java.util.*

/**
 * Create by munch1182 on 2021/4/9 15:29.
 */
class HeaderRvActivity : BaseTopActivity() {

    private val bind by bind<ActivityBaseRvBinding>(R.layout.activity_base_rv)
    private val model by get(AppItemViewModel::class.java)
    private lateinit var appAdapter: BaseBindAdapter<AppItemViewModel.AppItem, ItemHeaderRvBinding>

    private val usageRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { querySize() }

    private fun querySize() {
        model.appItemsSortByLetter.observeOnChanged(this) { data ->
            appAdapter.set(data.first)
            hideSrl()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        appAdapter = object :
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
            addItemDecoration(HeaderItemDecoration(this, true) { appAdapter.get(it) })
        }
        bind.baseTopSrl.apply { setOnRefreshListener { postDelayed({ hideSrl() }, 600L) } }
        showSrl()
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O || AppHelper.checkUsagePermission()) {
            querySize()
        } else {
            SimpleDialog.Normal(this)
                .setTitle("权限")
                .setContent("需要权限来查看应用数据")
                .setSureClickListener({
                    usageRequest.launch(IntentHelper.usageIntent())
                })
                .setCancelClickListener({ querySize() })
                .show()
        }
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