package com.munch.project.launcher.list

import android.os.Bundle
import android.view.View
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.FragmentListBinding

/**
 * Create by munch1182 on 2021/5/9 14:01.
 */
class ListFragment : BaseFragment() {

    private val bind by bind<FragmentListBinding>(R.layout.fragment_list)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.lifecycleOwner = this
    }
}