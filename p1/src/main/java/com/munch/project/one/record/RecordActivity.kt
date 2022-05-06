package com.munch.project.one.record

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cn.munch.lib.record.Record
import com.munch.lib.OnIndexListener
import com.munch.lib.extend.checkOnly
import com.munch.lib.extend.get
import com.munch.lib.extend.toDate
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.*
import com.munch.lib.recyclerview.BaseBindViewHolder
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter
import com.munch.lib.recyclerview.setOnItemClickListener
import com.munch.project.one.databinding.ItemRecordBinding
import com.munch.project.one.databinding.LayoutLogRecordBinding
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2022/4/19 17:04.
 */
class RecordActivity : BaseFastActivity(), ActivityDispatch by supportDef({ RecordDialog() }) {

    private val bind by fvHelperBindRv(RecordAdapter())
    private val vm by get<RecordVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = bind.adapter
        adapter.showRefresh()

        ItemTouchHelper(SwipedItemCallback {
            vm.del(adapter.get(it))
        }).attachToRecyclerView(bind.rv)

        vm.records().observe(this) { it?.let { adapter.set(it) } }

        adapter.setOnItemClickListener { _, pos, _ ->
            val get = adapter.get(pos)
            AlertDialog.Builder(this)
                .setMessage("${get?.log}\n${get?.thread}")
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog?.cancel() }
                .show()
        }
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

    @SuppressLint("SetTextI18n")
    class RecordDialog : ConfigDialog() {

        private val bind by add<LayoutLogRecordBinding>()
        private val vm by get<RecordVM>()

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            bind.container.checkOnly(typeOffsetIndex()) {
                updateQueryInput()
                vm.query.type = when (it) {
                    1 -> Record.TYPE_MSG
                    2 -> Record.TYPE_ERROR
                    3 -> Record.TYPE_EXCEPTION
                    4 -> Record.TYPE_TIME_MEASURE
                    5 -> Record.TYPE_OTHER
                    else -> -1
                }
                vm.query()

            }
            vm.count().observe(this) { bind.recordCount.text = "当前数量: ${it ?: 0}" }

            val time = vm.query.time
            if (time > 0L) {
                bind.recordDialogTime.setText(time.toDate())
                vm.query()
            }

            bind.recordDialogLike.setText(vm.query.like.replace("%", ""))
            dialog?.setOnCancelListener {
                updateQueryInput()
                vm.query()
            }

            vm.dbFrom().observe(this) { showFrom(it) }
            bind.recordReader.setOnClickListener { vm.changeFrom() }
        }

        private fun showFrom(it: Int?) {
            if (it == RecordVM.TYPE_READER) {
                bind.recordDbFrom.visibility = View.VISIBLE
                bind.recordDbFrom.text = "当前DB: reader"

                bind.recordReader.text = "SELF"
            } else {
                bind.recordDbFrom.visibility = View.GONE

                bind.recordReader.text = "READER"
            }
        }

        private fun typeOffsetIndex() = vm.query.type + 1

        private fun updateQueryInput() {
            bind.recordDialogTime.text.toString().trim().let {
                if (it.isEmpty()) {
                    null
                } else if (it.contains("-") && !it.contains(":")) { // 2020-04-20
                    "$it 00:00:00".toDate()
                } else if (!it.contains("-") && !it.contains(":")) {
                    if (it.length == 8) { // 20200420
                        ("${it.subSequence(0, 4)}" +
                                "-${it.subSequence(4, 6)}" +
                                "-${it.subSequence(6, 8)} " +
                                "00:00:00").toDate()
                    } else { // 20200420 000000
                        val sb = StringBuilder()
                        repeat(15 - it.length) { sb.append("0") }
                        sb.toString().toDate()
                    }
                } else if (it.length == 16) { // 2020-04-20 09:58
                    "$it:00".toDate()
                } else {
                    it.toDate()
                }
            }?.time?.let {
                vm.query.time = it
            }
            val like = bind.recordDialogLike.text.toString().trim()
            if (like.isNotEmpty()) {
                vm.query.like = "%$like%"
            }
        }

    }
}