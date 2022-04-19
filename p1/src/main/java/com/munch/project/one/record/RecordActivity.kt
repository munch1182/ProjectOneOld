package com.munch.project.one.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.munch.lib.record.Record
import cn.munch.lib.record.RecordHelper
import com.munch.lib.AppHelper
import com.munch.lib.extend.LinearLineItemDecoration
import com.munch.lib.extend.bind
import com.munch.lib.extend.toDate
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.SupportActionBar
import com.munch.lib.fast.view.SupportConfigDialog
import com.munch.lib.recyclerview.AdapterHelper
import com.munch.lib.recyclerview.BaseBindViewHolder
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter
import com.munch.project.one.databinding.ItemRecordBinding
import com.munch.project.one.databinding.LayoutRvOnlyBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/4/19 17:04.
 */
class RecordActivity : BaseFastActivity(),
    ActivityDispatch by (SupportActionBar + SupportConfigDialog()) {

    private val bind by bind<LayoutRvOnlyBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.rvOnly.apply {
            val lm = LinearLayoutManager(this@RecordActivity)
            layoutManager = lm
            addItemDecoration(LinearLineItemDecoration(lm))
        }
        val adapter = AdapterHelper(RecordAdapter()).bind(bind.rvOnly)

        adapter.showRefresh()

        /*ItemTouchHelper(object :ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        }).attachToRecyclerView(bind.rvOnly)*/

        lifecycleScope.launch(Dispatchers.IO) {
            RecordHelper.getInstance(AppHelper.app)
                .db.recordDao()
                .queryAllFlow()
                .collectLatest {
                    adapter.set(it)
                }
        }
    }

    private class RecordAdapter :
        BaseRecyclerViewAdapter<Record, BaseBindViewHolder<ItemRecordBinding>>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseBindViewHolder<ItemRecordBinding> {
            return BaseBindViewHolder(
                ItemRecordBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBind(
            holder: BaseBindViewHolder<ItemRecordBinding>,
            position: Int,
            bean: Record
        ) {
            holder.bind.apply {
                recordContent.text = bean.log
                recordTime.text = bean.recordTime.toDate()
                recordType.text = bean.type.toString()
            }
        }
    }
}