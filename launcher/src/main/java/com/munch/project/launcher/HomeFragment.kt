package com.munch.project.launcher

import android.os.Bundle
import android.view.View
import com.munch.lib.helper.BarHelper
import com.munch.project.launcher.databinding.ActivityMainBinding

/**
 * Create by munch1182 on 2022/4/3 17:03.
 */
class HomeFragment : BaseFragment() {
    private val bind by bind<ActivityMainBinding>()
    private val barHelper by lazy { BarHelper(requireActivity()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}