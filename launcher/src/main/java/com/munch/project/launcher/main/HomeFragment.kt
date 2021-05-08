package com.munch.project.launcher.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.FragmentHomeBinding

/**
 * Create by munch1182 on 2021/5/8 10:38.
 */
class HomeFragment : BaseFragment() {

    private val bind by bind<FragmentHomeBinding>(R.layout.fragment_home)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.homeTest.text = "123"
    }
}