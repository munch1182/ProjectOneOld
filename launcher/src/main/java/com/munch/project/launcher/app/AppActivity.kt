package com.munch.project.launcher.app

import android.os.Bundle
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import com.munch.lib.log
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivityAppBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Create by munch1182 on 2021/2/23 16:18.
 */
@AndroidEntryPoint
class AppActivity : BaseActivity() {

    private val binding by bind<ActivityAppBinding>(R.layout.activity_app)
    private val viewModel by viewModel<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        loadViewHelper.attachTarget(binding.appContainer).bind(this)

        viewModel.getAppList().observe(this) {
            log(it)
        }
    }
}