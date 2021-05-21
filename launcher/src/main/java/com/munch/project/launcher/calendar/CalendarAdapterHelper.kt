package com.munch.project.launcher.calendar

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
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

    fun getCalendarView(rv: RecyclerView): CalendarView? {
        val holder = rv.findViewHolderForItemId(CalendarAdapter.STABLE_ID) ?: return null
        return holder.itemView.findViewById(R.id.calender_view)
    }

    fun set(list: MutableList<Note>) {
        noteAdapter.set(list)
    }

    fun updateDay(day: Day, rv: RecyclerView) {
        getCalendarView(rv)?.update(day)
    }

    class NoteAdapter(private val scope: LifecycleCoroutineScope) :
        BaseDifferBindAdapter<Note, ItemNoteBinding>(
            R.layout.item_note,
            object : DiffUtil.ItemCallback<Note>() {
                override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                    return oldItem.note == newItem.note
                }

                override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                    return oldItem.color == newItem.color && oldItem.isFinished == newItem.isFinished
                }

            }
        ) {

        init {
            setHasStableIds(true)
            setOnItemLongClickListener { _, bean, _, _ ->
                scope.launch {
                    val newBean = bean.clone()
                    newBean.isFinished = !newBean.isFinished
                    if (newBean.isFinished) {
                        newBean.finishedTime = System.currentTimeMillis()
                    } else {
                        newBean.finishedTime = 0L
                    }
                    val newList = getNewList()
                    newList.remove(bean)
                    newList.add(newBean)
                    newList.sort()
                    set(newList)
                }
            }
        }

        override fun onBindViewHolder(
            holder: BaseBindViewHolder<ItemNoteBinding>,
            bean: Note,
            pos: Int
        ) {
            holder.bind.note = bean
            if (bean.isFinished) {
                holder.bind.noteCv.setCardBackgroundColor(Note.getColorFinished())
            } else {
                holder.bind.noteCv.setCardBackgroundColor(bean.color)
            }
        }

        override fun getItemId(position: Int) = getData()[position].hashCode().toLong()
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

