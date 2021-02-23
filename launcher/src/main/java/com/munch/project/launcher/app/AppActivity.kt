package com.munch.project.launcher.app

import android.os.Bundle
import androidx.core.view.get
import com.munch.lib.log
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivityAppBinding

/**
 * Create by munch1182 on 2021/2/23 16:18.
 */
class AppActivity : BaseActivity() {

    private val binding by bind<ActivityAppBinding>(R.layout.activity_app)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        loadViewHelper.attachTarget(binding.appContainer).bind(this)

        loadViewHelper.startLoadingInTime(3000L)

        for (i in 0 until binding.appContainer.childCount) {
            log(binding.appContainer.get(i))
        }

        binding.appNav.setAllLetters()
    }
}