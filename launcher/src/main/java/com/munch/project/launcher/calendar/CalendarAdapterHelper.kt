package com.munch.project.launcher.calendar

import android.content.Context
import android.content.res.ColorStateList
import androidx.recyclerview.widget.ConcatAdapter
import com.munch.pre.lib.base.rv.ItemDiffCallBack
import com.munch.pre.lib.calender.CalendarHelper
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseBindAdapter
import com.munch.project.launcher.base.BaseBindViewHolder
import com.munch.project.launcher.base.BaseDifferBindAdapter
import com.munch.project.launcher.base.StatusAdapter
import com.munch.project.launcher.databinding.ItemCalendarBinding
import com.munch.project.launcher.databinding.ItemNoteBinding

/**
 * Create by munch1182 on 2021/5/18 17:31.
 */
class CalendarAdapterHelper(context: Context) {

    private val noteAdapter = NoteAdapter()
    private val calendarAdapter = CalendarAdapter()

    fun getNoteAdapter() = noteAdapter

    private val adapter = ConcatAdapter(StatusAdapter(context), calendarAdapter, noteAdapter)

    fun getAdapter() = adapter
    fun getCalendarView() {
        throw UnsupportedOperationException("UNCOMPLETED")
    }

    fun set(list: MutableList<Note>) {
        noteAdapter.set(list)
    }

    class NoteAdapter : BaseDifferBindAdapter<Note, ItemNoteBinding>(
        R.layout.item_note, ItemDiffCallBack({ it.hashCode() })
    ) {

        init {
            setOnItemLongClickListener { adapter, bean, view, _ ->
                bean.isFinished = !bean.isFinished
                view.isSelected = !view.isSelected
                if (bean.isFinished) {
                    bean.finishedTime = System.currentTimeMillis()
                } else {
                    bean.finishedTime = 0L
                }
                adapter.sort()
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
    }

    class CalendarAdapter :
        BaseBindAdapter<Int, ItemCalendarBinding>(R.layout.item_calendar, mutableListOf(1)) {

        override fun onBindViewHolder(
            holder: BaseBindViewHolder<ItemCalendarBinding>,
            bean: Int,
            pos: Int
        ) {
            CalendarHelper.def(holder.bind.calenderView)
        }
    }
}

