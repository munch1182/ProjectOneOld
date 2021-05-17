package com.munch.project.launcher.home

import android.os.Bundle
import android.view.View
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.FragmentHomeBinding
import com.munch.project.launcher.item.AppActivity

/**
 * Create by munch1182 on 2021/5/8 10:38.
 */
class HomeFragment : BaseFragment() {

    private val bind by bind<FragmentHomeBinding>(R.layout.fragment_home)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.lifecycleOwner = this
        bind.homeContainer.setOnClickListener { AppActivity.start(requireActivity()) }
    }
}