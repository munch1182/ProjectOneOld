package com.munch.project.launcher.app

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.log
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivityAppBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

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

        val adapter = AppShowAdapterHelper(this)
        val spanCount = viewModel.getSpanCount().value!!
        val itemBean = adapter.getItemAdapter().getData()
        binding.appRv.apply {
            layoutManager =
                AppShowLayoutManager(this.context, spanCount, itemBean)
            this.adapter = adapter.getAdapter()
            addItemDecoration(NavItemDecoration(itemBean))
        }
        viewModel.getAppList().observe(this) { adapter.getItemAdapter().setData(it) }
        viewModel.getNavItems().observe(this) {
            binding.appNav.apply {
                setLetters(it.map { c -> c.key.toString().toUpperCase(Locale.ROOT) })
            }
        }

        syncScroll()
    }

    private fun syncScroll() {
        binding.appRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            val manager = binding.appRv.layoutManager as GridLayoutManager
            val selects = mutableListOf<String>()

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val nav = viewModel.getNavItems().value ?: return

                val first = AppShowAdapterHelper.offsetPos(manager.findFirstVisibleItemPosition())
                val last = AppShowAdapterHelper.offsetPos(manager.findLastVisibleItemPosition())
                selects.clear()
                run out@{
                    nav.forEach {
                        if (it.value in first..last) {
                            selects.add(it.key.toString().toUpperCase(Locale.ROOT))
                        }
                        if (it.value > last) {
                            return@out
                        }
                    }
                }
                binding.appNav.select(*selects.toTypedArray())
            }
        })
        binding.appNav.handleListener = { letter, rect ->
            binding.appRv.smoothScrollToPosition(
                AppShowAdapterHelper.resume2Pos(viewModel.getNavItems().value!![letter[0]] ?: 0)
            )
            true
        }
    }
}