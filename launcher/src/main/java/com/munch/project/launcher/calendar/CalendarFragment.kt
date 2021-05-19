package com.munch.project.launcher.calendar

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.FragmentCalenderBinding

/**
 * Create by munch1182 on 2021/5/8 11:22.
 */
class CalendarFragment : BaseFragment() {

    private val bind by bind<FragmentCalenderBinding>(R.layout.fragment_calender)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapterHelper = CalendarAdapterHelper(requireContext())
        bind.apply {
            calendarRv.layoutManager = LinearLayoutManager(requireContext())
            calendarRv.adapter = adapterHelper.getAdapter()
        }
    }
}