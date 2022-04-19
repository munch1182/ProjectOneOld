package com.munch.project.one.record

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cn.munch.lib.DBRecord
import cn.munch.lib.record.Record
import com.munch.lib.OnIndexListener
import com.munch.lib.extend.checkOnly
import com.munch.lib.extend.get
import com.munch.lib.extend.toDate
import com.munch.lib.extend.toLive
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.*
import com.munch.lib.recyclerview.BaseBindViewHolder
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter
import com.munch.project.one.databinding.ItemRecordBinding
import com.munch.project.one.databinding.LayoutLogRecordBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2022/4/19 17:04.
 */
class RecordActivity : BaseFastActivity(),
    ActivityDispatch by (SupportActionBar + SupportConfigDialog({ RecordDialog() })) {

    private val bind by fvHelperBindRv(RecordAdapter())
    private val vm by get<RecordVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = bind.adapter
        adapter.showRefresh()

        ItemTouchHelper(SwipedItemCallback { vm.del(adapter.get(it)) }).attachToRecyclerView(bind.rv)

        vm.records().observe(this) { adapter.set(it) }
    }

    private class RecordAdapter :
        BaseRecyclerViewAdapter<Record, BaseBindViewHolder<ItemRecordBinding>>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseBindViewHolder<ItemRecordBinding> {
            return BaseBindViewHolder(
                ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                recordType.text = bean.typeStr
            }
        }
    }

    private class SwipedItemCallback(
        private val onIndex: OnIndexListener
    ) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT, ItemTouchHelper.LEFT) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onIndex.invoke(viewHolder.bindingAdapterPosition)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val a = 1 - dX.absoluteValue / viewHolder.itemView.width
                viewHolder.itemView.alpha = a
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            viewHolder.itemView.alpha = 1f
            super.clearView(recyclerView, viewHolder)
        }
    }

    class RecordVM : ViewModel() {

        private val record = MutableLiveData<List<Record>>(emptyList())
        fun records() = record.toLive()
        private val count = MutableLiveData(0)
        fun count() = count.toLive()
        var type = 0
            private set

        fun filter(type: Int) {
            this.type = type
            viewModelScope.launch {
                if (type <= 0) {
                    record.postValue(DBRecord.queryAll())
                    count.postValue(DBRecord.querySize())
                } else {
                    record.postValue(DBRecord.queryByType(type))
                    count.postValue(DBRecord.querySizeBy(type))
                }
            }
        }

        fun del(r: Record?) {
            r ?: return
            viewModelScope.launch { DBRecord.del(r) }
        }

        init {
            viewModelScope.launch {
                //使用flow的方式会在数据库更新时自动传递数据
                DBRecord.queryAllFlow()
                    .collectLatest {
                        record.postValue(it)
                        count.postValue(DBRecord.querySize())
                    }
            }
        }
    }

    class RecordDialog : ConfigDialog() {

        private val bind by add<LayoutLogRecordBinding>()
        private val vm by get<RecordVM>()

        @SuppressLint("SetTextI18n")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            bind.container.checkOnly(vm.type) {
                vm.filter(
                    when (it) {
                        1 -> Record.TYPE_MSG
                        2 -> Record.TYPE_ERROR
                        3 -> Record.TYPE_EXCEPTION
                        4 -> Record.TYPE_TIME_MEASURE
                        5 -> Record.TYPE_OTHER
                        else -> -1
                    }
                )
            }
            vm.count().observe(this) {
                bind.recordCount.text = "当前数量: $it"
            }
        }

    }
}