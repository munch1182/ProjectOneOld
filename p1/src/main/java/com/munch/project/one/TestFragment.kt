package com.munch.project.one

import android.os.Bundle
import android.view.View
import com.munch.lib.extend.BindFragment
import com.munch.project.one.databinding.ActivityMainBinding

/**
 * Create by munch1182 on 2022/4/15 16:56.
 */
class TestFragment : BindFragment() {

    private val bind by bind<ActivityMainBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.text.setText(android.R.string.ok)
    }
}