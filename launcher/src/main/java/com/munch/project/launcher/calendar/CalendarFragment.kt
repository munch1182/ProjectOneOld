package com.munch.project.launcher.calendar

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.pre.lib.calender.Day
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.FragmentCalenderBinding
import java.util.*

/**
 * Create by munch1182 on 2021/5/8 11:22.
 */
class CalendarFragment : BaseFragment() {

    private val bind by bind<FragmentCalenderBinding>(R.layout.fragment_calender)
    private val adapterHelper by lazy { CalendarAdapterHelper(requireContext(), lifecycleScope) }
    private val calendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.apply {
            calendarRv.layoutManager = LinearLayoutManager(requireContext())
            calendarRv.adapter = adapterHelper.getAdapter()
        }
        adapterHelper.getNoteAdapter().set(MutableList(15) { Note.test() }.apply {
            sort()
        })
    }

    override fun onResume() {
        super.onResume()
        adapterHelper.updateDay(Day.from(calendar), bind.calendarRv)
    }
}