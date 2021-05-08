package com.munch.project.launcher.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.FragmentCalendarBinding

/**
 * Create by munch1182 on 2021/5/8 11:22.
 */
class CalendarFragment : BaseFragment() {

    private val bind by bind<FragmentCalendarBinding>(R.layout.fragment_calendar)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.homeTest.text = "333"
    }
}