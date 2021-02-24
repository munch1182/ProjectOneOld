package com.munch.project.launcher.app

import android.os.Bundle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.base.recyclerview.StatusBarAdapter
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

        val adapter = AppItemAdapter()
        val spanCount = viewModel.getSpanCount().value!!
        binding.appRv.apply {
            this.layoutManager =
                GridLayoutManager(this.context, spanCount).apply {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            //pos为0时是StatusBarAdapter
                            if (position == 0) {
                                return spanCount
                            }
                            return adapter.getData(position - 1).showParameter?.space2End!!
                        }
                    }
                }
            this.adapter = ConcatAdapter(StatusBarAdapter(this.context), adapter)
        }
        viewModel.getAppList().observe(this) { adapter.setData(it) }
    }
}