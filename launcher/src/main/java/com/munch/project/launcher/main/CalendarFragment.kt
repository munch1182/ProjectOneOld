package com.munch.project.launcher.main

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.helper.*
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.base.recyclerview.BaseSimpleBindAdapter
import com.munch.project.launcher.databinding.FragmentCalendarBinding
import com.munch.project.launcher.databinding.ItemCalendarBinding
import java.util.*

/**
 * Create by munch1182 on 2021/3/4 15:32.
 */
class CalendarFragment : BaseFragment<FragmentCalendarBinding>() {

    private val model by viewModel<CalendarViewModel>()

    override val resId: Int = R.layout.fragment_calendar
    private var lastChose = -1

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter =
            BaseSimpleBindAdapter<CalendarItem, ItemCalendarBinding>(R.layout.item_calendar)
            { holder, data, _ ->
                holder.binding.calendar = data
                holder.binding.root.isSelected = data.chose
                holder.binding.executePendingBindings()
            }.setOnItemClick { ada, _, data, pos ->
                if (lastChose == pos) {
                    return@setOnItemClick
                }
                if (lastChose != -1) {
                    ada.getData(lastChose).notChose()
                    ada.notifyItemChanged(lastChose)
                }
                lastChose = pos
                data.chose()
                ada.notifyItemChanged(pos)
                model.changeDay(data)
            }
        bind.calendarRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 7).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        val data = adapter.getData(position)
                        if (data.isEmpty) {
                            return data.span
                        }
                        return 1
                    }
                }
            }
            this.adapter = adapter
            addItemDecoration(CalendarItemDecoration(this))
        }
        model.getMouth().observe(viewLifecycleOwner) {
            adapter.setData(it)
        }
        model.getChose().observe(viewLifecycleOwner) {
            bind.calendarTvDay.text = "M月d日".formatDate(it)
            bind.calendarTvDayDis.text = "${"yyyy".formatDate(it)} ${
                DateHelper.getDateStr2Now(
                    it.time,
                    DateUtils.FORMAT_ABBREV_RELATIVE,
                    DateUtils.DAY_IN_MILLIS
                )
            }"
        }
        bind.calendarTvDay.setOnClickListener {
            changeMoth()
        }

    }

    override fun onResume() {
        super.onResume()
        model.resetToday()
    }

    private fun changeMoth() {
        model.nextMouth()
        lastChose = -1
    }

    class CalendarItemDecoration(private val parent: RecyclerView) : RecyclerView.ItemDecoration() {

        private var height = 80
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 40F
            color = parent.context.getColorCompat(R.color.colorTextGray)
        }

        init {
            parent.addPadding(t = height)
        }

        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDrawOver(c, parent, state)
            val width = parent.measuredWidth - parent.paddingLeft - parent.paddingRight

            val space = width / 7
            repeat(7) {
                c.drawTextInCenter(
                    getStrFromWeek(it),
                    (space * it + space / 2).toFloat(),
                    height.toFloat() / 2f, paint
                )
            }
        }

        private fun getStrFromWeek(week: Int): String {
            return when (week) {
                0 -> "一"
                1 -> "二"
                2 -> "三"
                3 -> "四"
                4 -> "五"
                5 -> "六"
                6 -> "天"
                else -> ""
            }
        }
    }

    class CalendarViewModel : ViewModel() {

        private val instance = Calendar.getInstance()
        private val allItem = hashMapOf<Int, MutableList<CalendarItem>>()
        private val mouthList = mutableListOf<CalendarItem>()
        private var startDay = Calendar.MONDAY
        private var endDay = Calendar.SUNDAY

        private val mouthLiveDate = MutableLiveData(mouthList)
        fun getMouth(): LiveData<MutableList<CalendarItem>> = mouthLiveDate
        private val choseLiveData = MutableLiveData(instance.time)
        fun getChose(): LiveData<Date> = choseLiveData

        init {
            instance.firstDayOfWeek = startDay
            instance.minimalDaysInFirstWeek = 7

            updateMouthList()
        }

        fun updateMouthList() {
            updateMouthList(instance.get(Calendar.YEAR), instance.get(Calendar.MONTH))
        }

        fun updateMouthList(year: Int, mouth: Int) {
            val key = key2Mouth(year, mouth)
            val list = allItem[key]
            mouthList.clear()

            if (list.isNullOrEmpty()) {

                instance.set(year, mouth, 1)
                val span = instance.get(Calendar.DAY_OF_WEEK)
                if (span != startDay) {
                    if (span == Calendar.SUNDAY) {
                        mouthList.add(CalendarItem.empty(6))
                    } else {
                        mouthList.add(CalendarItem.empty(span - startDay))
                    }
                }
                instance.add(Calendar.MONTH, 1)
                instance.add(Calendar.DAY_OF_MONTH, -1)
                for (i in 1..instance.get(Calendar.DAY_OF_MONTH)) {
                    mouthList.add(CalendarItem(i.toString()))
                }
                allItem[key] = MutableList(mouthList.size) { mouthList[it] }
            } else {
                mouthList.addAll(list)
            }
            mouthLiveDate.postValue(mouthList)
        }

        fun key2Mouth(year: Int, mouth: Int): Int {
            return year * 100 + mouth
        }

        fun getYearAndMouth(key: Int): Pair<Int, Int> {
            return Pair(key / 100, key % 100)
        }

        fun nextMouth() {
            //重置为当前第一天
            instance.set(Calendar.DAY_OF_MONTH, 1)
            //添加一个月到下一个月第一天
            instance.add(Calendar.MONTH, 1)
            updateMouthList()
            choseLiveData.postValue(instance.time)
        }

        fun changeDay(data: CalendarItem) {
            if (data.day.isEmpty()) {
                return
            }
            instance.set(Calendar.DAY_OF_MONTH, data.day.toInt())
            choseLiveData.postValue(instance.time)
        }

        fun resetToday() {
            instance.time = Date()
            updateMouthList()
        }
    }

    data class CalendarItem(
        val day: String,
        val content: String? = null,
        //是否是当前选中
        var chose: Boolean = false,
        //特殊工作或者休息安排
        var isSpiceWork: Boolean = false,
        val isEmpty: Boolean = false,
        val span: Int = 0
    ) {
        fun chose() {
            chose = true
        }

        fun notChose() {
            chose = false
        }

        companion object {

            fun empty(span: Int): CalendarItem {
                return CalendarItem("", isEmpty = true, span = span)
            }
        }
    }
}