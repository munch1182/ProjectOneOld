package com.munch.project.one.record

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.DBRecord
import com.munch.lib.OnIndexListener
import com.munch.lib.extend.*
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.*
import com.munch.lib.helper.FileHelper
import com.munch.lib.helper.new
import com.munch.lib.record.Record
import com.munch.lib.recyclerview.BaseBindViewHolder
import com.munch.lib.recyclerview.BindRVAdapter
import com.munch.lib.recyclerview.setOnItemClickListener
import com.munch.project.one.databinding.ItemRecordBinding
import com.munch.project.one.databinding.LayoutLogRecordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2022/4/19 17:04.
 */
class RecordActivity : BaseFastActivity(),
    ActivityDispatch by (SupportShareActionBar + SupportConfigDialog({ RecordDialog() })) {

    private val bind by fvHelperBindRv(RecordAdapter())
    private val vm by get<RecordVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = bind.adapter
        adapter.showRefresh()

        ItemTouchHelper(SwipedItemCallback {
            vm.dispatch(QueryIntent.Del(adapter.get(it) ?: return@SwipedItemCallback))
        }).attachToRecyclerView(bind.rv)


        vm.uiState.observe(this) {
            when (it) {
                RecordUIState.Querying -> adapter.showRefresh()
                is RecordUIState.Data -> adapter.set(it.data)
                is RecordUIState.Error -> adapter.showEmpty()
            }
        }

        adapter.setOnItemClickListener { _, pos, _ ->
            val get = adapter.get(pos)
            AlertDialog.Builder(this)
                .setMessage("${get?.log}\n${get?.thread}")
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog?.cancel() }
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (ISupportShareActionBar.isShare(item)) {
            lifecycleScope.launch(Dispatchers.IO) {
                val name = "log/${System.currentTimeMillis().toDateStr("yyyyMMddHHmmss")}.txt"
                val file = File(cacheDir, name)
                file.new()
                FileOutputStream(file).use { DBRecord.share2File(it) }
                FileHelper.toUri(ctx, file)?.let { shareUri(it) }
            }
            return true
        }
        return super<BaseFastActivity>.onOptionsItemSelected(item)
    }

    private class RecordAdapter : BindRVAdapter<Record, ItemRecordBinding>() {

        override fun onBind(
            holder: BaseBindViewHolder<ItemRecordBinding>,
            bean: Record
        ) {
            holder.bind.apply {
                recordContent.text = bean.log
                recordTime.text = bean.recordTime.toDateStr()
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

    internal class RecordDialog : ConfigDialog() {

        private val bind by add<LayoutLogRecordBinding>()
        private val vm by get<RecordVM>()
        private var query = RecordQuery()

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            dialog?.setOnCancelListener { query() }

            bind.recordClear.setOnClickListener { vm.dispatch(QueryIntent.Clear) }
            bind.recordQuery.setOnClickListener { query() }

            vm.uiState.observe(this) {
                if (it is RecordUIState.Data) {
                    showQuery(it.query)
                }
            }
        }

        private fun query() {
            updateQueryInput()
            vm.dispatch(QueryIntent.Query(query))
        }

        @SuppressLint("SetTextI18n")
        private fun showQuery(change: Change) {
            query = change.query

            bind.container.checkOnly(typeOffsetIndex(query.type)) {
                query.type = when (it) {
                    1 -> Record.TYPE_MSG
                    2 -> Record.TYPE_ERROR
                    3 -> Record.TYPE_EXCEPTION
                    5 -> Record.TYPE_TIME_MEASURE
                    4 -> Record.TYPE_OTHER
                    else -> -1
                }
                vm.dispatch(QueryIntent.Query(query))
            }
            val page = query.page
            if (page > 0) {
                bind.recordDialogPage.setText(page.toString())
            }

            val time = query.time
            if (time > 0) {
                bind.recordDialogTime.setText(time.toDateStr("yyyyMMddHHmmss"))
            }

            val like = query.like.removePrefix("%").removeSuffix("%")
            if (like.isNotEmpty()) {
                bind.recordDialogLike.setText(like)
            }

            bind.recordDialogPage.setText(query.page.toString())
            bind.recordDialogSize.setText(query.size.toString())
            bind.recordCount.text = "当前数量: ${change.count}"
        }

        private fun typeOffsetIndex(type: Int) = type + 1

        private fun updateQueryInput() {
            val timeStr = bind.recordDialogTime.text.toString().trim()
            if (timeStr.length in 1..14) {
                val sb = StringBuilder(timeStr)
                repeat(14 - timeStr.length) { sb.append("0") }
                query.time = sb.toString().toDateStr("yyyyMMddHHmmss")?.time ?: 0
            } else {
                query.time = 0
            }
            val like = bind.recordDialogLike.text.toString().trim()
            if (like.isNotEmpty()) {
                query.like = "%$like%"
            } else {
                query.like = ""
            }
            try {
                val trim = bind.recordDialogPage.text.toString().trim()
                if (trim.isNotEmpty()) {
                    query.page = trim.toInt()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val trim = bind.recordDialogSize.text.toString().trim()
                if (trim.isNotEmpty()) {
                    query.size = trim.toInt()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            bind.container.clearFocusAll()
        }
    }
}