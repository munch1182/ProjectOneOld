package com.munch.project.launcher.calendar

import android.content.Context
import android.content.res.ColorStateList
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.munch.pre.lib.base.rv.ItemDiffCallBack
import com.munch.pre.lib.calender.CalendarHelper
import com.munch.pre.lib.calender.CalendarView
import com.munch.pre.lib.calender.Day
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseBindAdapter
import com.munch.project.launcher.base.BaseBindViewHolder
import com.munch.project.launcher.base.BaseDifferBindAdapter
import com.munch.project.launcher.base.StatusAdapter
import com.munch.project.launcher.databinding.ItemCalendarBinding
import com.munch.project.launcher.databinding.ItemNoteBinding
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/5/18 17:31.
 */
class CalendarAdapterHelper(context: Context, scope: LifecycleCoroutineScope) {

    private val noteAdapter = NoteAdapter(scope)
    private val calendarAdapter = CalendarAdapter()

    fun getNoteAdapter() = noteAdapter

    private val adapter = ConcatAdapter(
        ConcatAdapter.Config.Builder()
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.SHARED_STABLE_IDS)
            .build(),
        StatusAdapter(context), calendarAdapter, noteAdapter
    )

    fun getAdapter() = adapter
    fun getCalendarView() {
        throw UnsupportedOperationException("UNCOMPLETED")
    }

    fun set(list: MutableList<Note>) {
        noteAdapter.set(list)
    }

    fun updateDay(day: Day, rv: RecyclerView) {
        val holder = rv.findViewHolderForItemId(CalendarAdapter.STABLE_ID) ?: return
        holder.itemView.findViewById<CalendarView>(R.id.calender_view)?.update(day)
    }

    class NoteAdapter(private val scope: LifecycleCoroutineScope) :
        BaseDifferBindAdapter<Note, ItemNoteBinding>(
            R.layout.item_note,
            ItemDiffCallBack({ it.note }, { it.finishedTime })
        ) {

        init {
            setHasStableIds(true)
            setOnItemLongClickListener { adapter, bean, _, _ ->
                scope.launch {
                    bean.isFinished = !bean.isFinished
                    if (bean.isFinished) {
                        bean.finishedTime = System.currentTimeMillis()
                    } else {
                        bean.finishedTime = 0L
                    }
                    adapter.sort()
                }
            }
        }

        override fun onBindViewHolder(
            holder: BaseBindViewHolder<ItemNoteBinding>,
            bean: Note,
            pos: Int
        ) {
            holder.bind.note = bean
            holder.bind.noteCv.apply {
                isSelected = bean.isFinished
                setCardBackgroundColor(
                    ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_selected),
                            intArrayOf(-android.R.attr.state_selected)
                        ),
                        intArrayOf(Note.getColorFinished(), bean.color)
                    )
                )
            }
        }

        override fun getItemId(position: Int): Long {
            return 1000L + position
        }
    }

    class CalendarAdapter :
        BaseBindAdapter<Int, ItemCalendarBinding>(R.layout.item_calendar, mutableListOf(1)) {

        companion object {

            const val STABLE_ID = 100L
        }

        init {
            setHasStableIds(true)
        }

        private var day = Day.now()

        override fun onBindViewHolder(
            holder: BaseBindViewHolder<ItemCalendarBinding>,
            bean: Int,
            pos: Int
        ) {
            CalendarHelper.def(holder.bind.calenderView, day)
        }

        override fun getItemId(position: Int): Long {
            return STABLE_ID
        }
    }
}

