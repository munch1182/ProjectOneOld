package com.munch.project.launcher.main

import android.os.Bundle
import android.view.View
import com.munch.project.launcher.MainActivity
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.FragmentHomeBinding

/**
 * Create by munch1182 on 2021/3/4 15:04.
 */
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override val resId: Int = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.mainBtnApp.setOnClickListener {
            (activity as MainActivity).startAppActivity()
        }
    }
}