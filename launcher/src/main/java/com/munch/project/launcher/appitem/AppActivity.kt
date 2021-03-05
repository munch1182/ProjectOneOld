package com.munch.project.launcher.appitem

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.base.Orientation
import com.munch.lib.helper.*
import com.munch.project.launcher.R
import com.munch.project.launcher.app.task.AppItemHelper
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.base.DialogHelper
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
    private val swipeViewHelper by lazy { SwipeViewHelper(this) }

    companion object {

        fun start(context: Activity) {
            context.startActivity(AppActivity::class.java)
            context.overridePendingTransition(R.anim.anim_enter_up, 0)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        swipeSet()
        loadViewHelper.attachTarget(binding.appContainer).bind(this)

        rvSet()
        viewModel.getNavItems().observe(this) {
            binding.appNav.setLetters(it.map { c -> c.key.toString().toUpperCase(Locale.ROOT) })
        }
        //当应用安装或卸载并且处于本页面时需要手动触发更新
        AppItemHelper.getInstance().isChange().observe(this, { viewModel.updateAppShow() })
        syncScroll()
    }

    private fun rvSet() {
        val adapter = AppShowAdapterHelper(this)
        val spanCount = viewModel.getSpanCount().value!!
        val itemBean = adapter.getItemAdapter().getData()
        adapter.getItemAdapter().setOnItemClick { _, _, data, _ -> openApp(data) }
        adapter.getItemAdapter().setOnItemLongClick { _, _, data, _ ->
            val pkgName = data.appBean.pkgName
            if (pkgName == packageName) {
                return@setOnItemLongClick true
            }
            DialogHelper.Center(this).set {
                setTitle("卸载应用")
                setMessage("卸载${data.appBean.name}?")
                setPositiveButton("卸载") { _, _ ->
                    //由广播监听触发更新，而不是requestCode
                    AppHelper.uninstall(this@AppActivity, pkgName)
                }
            }.show()
            true
        }
        binding.appRv.apply {
            layoutManager = AppShowLayoutManager(this.context, spanCount, itemBean)
            this.adapter = adapter.getAdapter()
            addItemDecoration(NavItemDecoration(itemBean))
        }
        viewModel.getAppList().observe(this) { adapter.getItemAdapter().setData(it) }
    }

    private fun swipeSet() {
        swipeViewHelper.setActivity()
        val firstAlpha = binding.appContainer.alpha
        val interpolator = LinearInterpolator()
        val animTime = resources.getInteger(R.integer.int_time_activity_anim).toLong()
        swipeViewHelper.getSwipeView()
            .orientation(Orientation.TB)
            .animHandle {
                it.duration = animTime
                it.interpolator = interpolator
            }
            .process { _, _, process ->
                var processTemp = firstAlpha - process
                if (processTemp > firstAlpha) {
                    processTemp = firstAlpha
                } else if (processTemp < 0f) {
                    processTemp = 0f
                }
                binding.appContainer.alpha = processTemp
                if (processTemp <= 0.2f) {
                    onBackPressed()
                }

            }
    }

    override fun fitStatus(contentView: View?, params: ViewGroup.LayoutParams?) {
        //not fit
        /*super.fitStatus(contentView, params)*/
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

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                swipeViewHelper.getSwipeView()
                    .enable(manager.findFirstVisibleItemPosition() == 0)
            }
        })
        binding.appNav.handleListener = { letter, rect ->
            swipeViewHelper.getSwipeView().enable(false)
            var marginBottom = dp48
            var marginTop = rect.top + rect.height() / 2 - dp80 / 2 + dp48
            if (marginTop < 0) {
                marginBottom = marginTop.absoluteValue
                marginTop = 0
            }
            binding.appTeardrop.setProperty {
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
            swipeViewHelper.getSwipeView().enable(true)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.anim_exit_down)
    }
}