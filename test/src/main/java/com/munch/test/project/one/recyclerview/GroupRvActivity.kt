package com.munch.test.project.one.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.extend.observeOnChanged
import com.munch.pre.lib.helper.drawTextInYCenter
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityGroupRvBinding
import com.munch.test.project.one.databinding.ItemGroupRvBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Create by munch1182 on 2021/4/9 15:32.
 */
class GroupRvActivity : BaseTopActivity() {

    private val bind by bind<ActivityGroupRvBinding>(R.layout.activity_group_rv)
    private val model by get(AppItemViewModel::class.java)
    private lateinit var appAdapter: BaseBindAdapter<AppItemViewModel.AppGroupItem, ItemGroupRvBinding>
    private val spanCount = 4
    private val manager by lazy { bind.baseTopRv.layoutManager as GridLayoutManager }
    private val smoothScroller by lazy { TopSmoothScroller(this) }

    private fun querySize() {
        model.span(spanCount).appItemSortByGroup.observeOnChanged(this) { data ->
            appAdapter.set(data.first)
            val map = data.second
            val letter = runBlocking {
                withContext(lifecycleScope.coroutineContext + Dispatchers.Default) {
                    map.keys.map { it.toString() }.toMutableList().apply { sort() }
                }
            }
            bind.groupLetter.clickListener = { s, _ ->
                val pos = map[s.toCharArray()[0]]
                if (pos != null) {
                    smooth2Pos(pos)
                }
            }
            bind.groupLetter.setLetters(letter)
            selectRange()
            hideSrl()
        }
    }

    private fun smooth2Pos(pos: Int) {
        smoothScroller.targetPosition = pos
        manager.startSmoothScroll(smoothScroller)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        appAdapter = object :
            BaseBindAdapter<AppItemViewModel.AppGroupItem, ItemGroupRvBinding>(R.layout.item_group_rv) {

            override fun onBindViewHolder(
                holder: BaseBindViewHolder<ItemGroupRvBinding>,
                bean: AppItemViewModel.AppGroupItem,
                pos: Int
            ) {
                holder.bind.app = bean
            }
        }
        bind.baseTopRv.apply {
            layoutManager = GridLayoutManager(this@GroupRvActivity, spanCount).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return appAdapter.get(position).span2End
                    }
                }
            }
            setBackgroundColor(Color.WHITE)
            setPadding(120, 0, 0, 0)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 50f
                    color = Color.BLACK
                }
                private val padding = resources.getDimension(R.dimen.padding_def)
                private val manager = layoutManager as LinearLayoutManager
                override fun onDrawOver(
                    c: Canvas,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.onDrawOver(c, parent, state)
                    val firstPos = manager.findFirstVisibleItemPosition()
                    val endPos = manager.findLastVisibleItemPosition()
                    if (firstPos == -1 || endPos == -1) {
                        return
                    }
                    for (i in firstPos..endPos) {
                        if (!appAdapter.hasIndex(i)) {
                            return
                        }
                        val bean = appAdapter.get(i)
                        if (bean.indexInLetter == 0) {
                            val view = parent.getChildAt(i - firstPos) ?: return
                            c.drawTextInYCenter(
                                bean.char.toString(), padding,
                                view.top + padding + 50f / 2f, paint
                            )
                        }
                    }
                }
            })
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    selectRange()
                }
            })
            adapter = appAdapter
        }
        bind.baseTopSrl.apply { setOnRefreshListener { postDelayed({ hideSrl() }, 600L) } }
        showSrl()
        querySize()
    }

    private fun selectRange() {
        val firstPos = manager.findFirstVisibleItemPosition()
        val lastPos = manager.findLastVisibleItemPosition()
        if (firstPos == -1 || lastPos == -1) {
            return
        }
        if (!appAdapter.hasIndex(firstPos) || !appAdapter.hasIndex(lastPos)){
            return
        }
        val firstBean = appAdapter.get(firstPos)
        val lastBean = appAdapter.get(lastPos)
        bind.groupLetter.selectRange(
            firstBean.char.toString(),
            lastBean.char.toString()
        )
    }

    private fun showSrl() {
        bind.baseTopSrl.isRefreshing = true
    }

    private fun hideSrl() {
        //错开更新的时间
        bind.baseTopSrl.apply {
            postDelayed({ isRefreshing = false }, 500L)
        }
    }

    class TopSmoothScroller(context: Context) : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }
}