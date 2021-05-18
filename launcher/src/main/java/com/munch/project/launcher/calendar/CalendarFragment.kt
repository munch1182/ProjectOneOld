package com.munch.project.launcher.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.FragmentCalenderBinding

/**
 * Create by munch1182 on 2021/5/8 11:22.
 */
class CalendarFragment : BaseFragment() {

    private val bind by bind<FragmentCalenderBinding>(R.layout.fragment_calender)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.lifecycleOwner = this
    }
}