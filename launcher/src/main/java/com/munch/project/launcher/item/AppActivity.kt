package com.munch.project.launcher.item

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.munch.pre.lib.base.Orientation
import com.munch.pre.lib.base.rv.ItemDiffCallBack
import com.munch.pre.lib.extend.dp2Px
import com.munch.pre.lib.extend.observeOnChanged
import com.munch.pre.lib.extend.startActivity
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.BarHelper
import com.munch.pre.lib.helper.SwipeViewHelper
import com.munch.pre.lib.helper.drawTextInYCenter
import com.munch.project.launcher.R
import com.munch.project.launcher.base.*
import com.munch.project.launcher.databinding.ActivityAppBinding
import com.munch.project.launcher.databinding.ItemAppItemBinding
import com.munch.project.launcher.extend.bind
import com.munch.project.launcher.extend.get
import kotlinx.coroutines.launch
import java.util.*

/**
 * Create by munch1182 on 2021/5/8 11:43.
 */
class AppActivity : BaseActivity() {

    companion object {

        fun start(context: Activity) {
            context.startActivity(AppActivity::class.java)
            context.overridePendingTransition(R.anim.anim_enter_up, 0)
        }
    }

    private val bind by bind<ActivityAppBinding>(R.layout.activity_app)
    private val model by get(AppViewModel::class.java)
    private val swipeViewHelper by lazy { SwipeViewHelper(this) }
    private val smoothScroller by lazy { TopSmoothScroller(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this

        val spanCount = model.getSpanCount().value!!
        val adapterHelper = AppAdapterHelper(this)
        val gridLayoutManager = GridLayoutManager(this, spanCount).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) =
                    adapterHelper.get(position)?.span2End ?: spanCount
            }
        }
        bind.itemRv.apply {
            layoutManager = gridLayoutManager
            setBackgroundColor(Color.WHITE)
            setPadding(100, 0, 100, 0)
            addItemDecoration(ItemDecoration(bind.itemRv, adapterHelper))
            adapter = adapterHelper.getAdapter()
        }
        setSwipe()
        syncScroll()
        model.getItems().observeOnChanged(this) {
            adapterHelper.set(it.first)
            handleLetter(it.second)
        }
        model.getSpanCount().observeOnChanged(this) { gridLayoutManager.spanCount = it }

        LauncherApp.getInstance().appUpdate = { model.update() }
    }

    override fun onDestroy() {
        super.onDestroy()
        LauncherApp.getInstance().appUpdate = null
    }

    override fun handleBar() {
        /*super.handleBar()*/
        BarHelper(this).hideStatusBar(true).colorStatusBar(Color.TRANSPARENT)
        window.navigationBarColor = Color.WHITE
    }

    private fun syncScroll() {
        bind.itemRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            val manager = bind.itemRv.layoutManager as GridLayoutManager
            val selects = mutableListOf<String>()
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val nav = model.getItems().value?.second ?: return
                selects.clear()

                var first = manager.findFirstCompletelyVisibleItemPosition()
                when (first) {
                    -1 -> return
                    0 -> first = 0
                    else -> first -= 1
                }
                val last = manager.findLastVisibleItemPosition() - 1

                kotlin.run {
                    nav.forEach {
                        if (it.value in first..last) {
                            selects.add(it.key.toString().toUpperCase(Locale.getDefault()))
                        }
                        if (it.value > last) {
                            return@run
                        }
                    }
                }
                bind.itemLetter.select(*selects.toTypedArray())
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                swipeViewHelper.getSwipeView().enable = manager.findFirstVisibleItemPosition() == 0
            }
        })
    }

    private fun handleLetter(map: HashMap<Char, Int>) {
        lifecycleScope.launch {
            bind.itemLetter.setLetters(map.map {
                it.key.toString().toUpperCase(Locale.getDefault())
            }.toMutableList())
        }
        //itemTeardrop的高度的一半
        val dimen30 = dp2Px(60f) / 2
        bind.itemLetter.handleListener = { letter, rect ->
            swipeViewHelper.getSwipeView().enable = false
            val pos = map[letter.toCharArray()[0]]
            if (pos != null) {
                smooth2Pos(pos)
            }
            bind.itemTeardrop.apply {
                if (text != letter) {
                    this.visibility = View.VISIBLE
                    text = letter
                    if (layoutParams is ConstraintLayout.LayoutParams) {
                        val i = rect.top + (rect.bottom - rect.top) / 2 - dimen30
                        (layoutParams as ConstraintLayout.LayoutParams).setMargins(
                            0, i.toInt(), 0, 0
                        )
                    }
                    requestLayout()
                    invalidate()
                }
            }
            true
        }
        bind.itemLetter.selectEndListener = { _, _ ->
            swipeViewHelper.getSwipeView().enable = true
            bind.itemTeardrop.postDelayed(
                { bind.itemTeardrop.visibility = View.GONE }, 750L
            )
        }
    }

    private fun smooth2Pos(pos: Int) {
        smoothScroller.targetPosition = pos
        bind.itemRv.layoutManager?.startSmoothScroll(smoothScroller)
    }

    private fun setSwipe() {
        val firstAlpha = bind.appContainer.alpha
        swipeViewHelper.apply {
            getSwipeView().orientation(Orientation.TB).apply {
                processListener = {
                    var processTemp = firstAlpha - it
                    if (processTemp > firstAlpha) {
                        processTemp = firstAlpha
                    } else if (processTemp < 0f) {
                        processTemp = 0f
                    }
                    bind.appContainer.alpha = processTemp
                }
                animEndListener = { if (it) onBackPressed() }
            }
            setActivity()
        }
    }

    override fun toggleTheme() {
        //不实现
        /*super.toggleTheme()*/
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.anim_exit_down)
    }

    private fun refresh() {
        model.refresh()
    }

    private inner class AppAdapterHelper(context: Context) {
        private val appAdapter = AppAdapter().apply {
            setOnItemClickListener { _, bean, _, _ ->
                if (bean.launch.isEmpty()) {
                    refresh()
                } else {
                    context.startActivity(
                        Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setComponent(ComponentName(bean.pkg, bean.launch))
                    )
                }
            }
            setOnItemLongClickListener { _, bean, _, _ ->
                AppHelper.uninstall(this@AppActivity, bean.pkg)
            }
        }
        private val statusAdapter = StatusAdapter(context)
        private val adapter = ConcatAdapter(statusAdapter, appAdapter)

        fun getAdapter() = adapter

        fun get(position: Int): AppGroupItem? {
            if (position < statusAdapter.itemCount) {
                return null
            }
            return appAdapter.get(position - statusAdapter.itemCount)
        }

        fun set(list: MutableList<AppGroupItem>) {
            appAdapter.set(list)
        }
    }

    private class AppAdapter :
        BaseDifferBindAdapter<AppGroupItem, ItemAppItemBinding>(
            R.layout.item_app_item,
            ItemDiffCallBack({ it.pkg }, { it.name })
        ) {

        override fun onBindViewHolder(
            holder: BaseBindViewHolder<ItemAppItemBinding>,
            bean: AppGroupItem,
            pos: Int
        ) {
            holder.bind.app = bean
        }
    }

    private class ItemDecoration(rv: RecyclerView, private val helper: AppAdapterHelper) :
        RecyclerView.ItemDecoration() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 50f
            color = Color.BLACK
        }
        private val padding = rv.context.resources.getDimension(R.dimen.padding_def)
        private val manager = rv.layoutManager as LinearLayoutManager

        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDrawOver(c, parent, state)
            val firstPos = manager.findFirstVisibleItemPosition()
            val endPos = manager.findLastVisibleItemPosition()
            if (firstPos == -1 || endPos == -1) {
                return
            }
            for (i in firstPos..endPos) {
                val bean = helper.get(i) ?: continue
                if (bean.indexInLetter == 0) {
                    val view = parent.getChildAt(i - firstPos) ?: return
                    c.drawTextInYCenter(
                        bean.letter.toString(), padding,
                        view.top + padding + 50f, paint
                    )
                }
            }
        }
    }

    private class TopSmoothScroller(context: Context) : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }

}