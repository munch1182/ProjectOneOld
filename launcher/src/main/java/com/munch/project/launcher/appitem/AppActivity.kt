package com.munch.project.launcher.appitem

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.helper.dp2Px
import com.munch.lib.helper.setMargin
import com.munch.lib.helper.startActivity
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivityAppBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2021/2/23 16:18.
 */
@AndroidEntryPoint
class AppActivity : BaseActivity() {

    private val binding by bind<ActivityAppBinding>(R.layout.activity_app)
    private val viewModel by viewModel<AppViewModel>()
    private val dp48 by lazy { dp2Px(48f).toInt() }
    private val dp80 by lazy { dp2Px(80f).toInt() }

    companion object {

        fun start(context: Activity) {
            context.startActivity(AppActivity::class.java)
            context.overridePendingTransition(R.anim.anim_enter_up, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        loadViewHelper.attachTarget(binding.appContainer).bind(this)

        val adapter = AppShowAdapterHelper(this)
        val spanCount = viewModel.getSpanCount().value!!
        val itemBean = adapter.getItemAdapter().getData()
        adapter.getItemAdapter().setOnItemClick { _, _, data, _ ->
            openApp(data)
        }
        binding.appRv.apply {
            layoutManager = AppShowLayoutManager(this.context, spanCount, itemBean)
            this.adapter = adapter.getAdapter()
            addItemDecoration(NavItemDecoration(itemBean))
        }
        viewModel.getAppList().observe(this) { adapter.getItemAdapter().setData(it) }
        viewModel.getNavItems().observe(this) {
            binding.appNav.setLetters(it.map { c -> c.key.toString().toUpperCase(Locale.ROOT) })
        }

        syncScroll()
    }

    private fun openApp(data: AppShowBean) {
        val launch = data.appBean.launcherActivity ?: return
        startActivity(
            Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setComponent(ComponentName(data.appBean.pkgName, launch))
        )
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

            var marginBottom = dp48
            var marginTop = rect.top + rect.height() / 2 - dp80 / 2 + dp48
            if (marginTop < 0) {
                marginBottom = marginTop.absoluteValue
                marginTop = 0
            }
            binding.appTeardrop.apply {
                binding.appTeardrop.text = letter
                val params = binding.appTeardrop.layoutParams
                if (params is ConstraintLayout.LayoutParams) {
                    setMargin(params.leftMargin, marginTop, params.rightMargin, marginBottom)
                }
                visibility = View.VISIBLE
            }
            binding.appRv.smoothScrollToPosition(
                AppShowAdapterHelper.resume2Pos(viewModel.getNavItems().value!![letter[0]] ?: 0)
            )
            true
        }
        binding.appNav.selectEndListener = { _, _ ->
            binding.appTeardrop.apply {
                postDelayed({ this.visibility = View.GONE }, 300L)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.anim_exit_down)
    }
}