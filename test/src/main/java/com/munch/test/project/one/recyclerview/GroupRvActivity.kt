package com.munch.test.project.one.recyclerview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.databinding.ActivityBaseRvBinding
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.base.rv.DiffItemCallback
import com.munch.pre.lib.extend.observeOnChanged
import com.munch.pre.lib.helper.drawTextInYCenter
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ItemGroupRvBinding

/**
 * Create by munch1182 on 2021/4/9 15:32.
 */
class GroupRvActivity : BaseTopActivity() {

    private val bind by bind<ActivityBaseRvBinding>(R.layout.activity_base_rv)
    private val model by get(AppItemViewModel::class.java)
    private lateinit var appAdapter: BaseBindAdapter<AppItemViewModel.AppGroupItem, ItemGroupRvBinding>
    private val spanCount = 4

    private fun querySize() {
        model.span(spanCount).appItemSortByGroup.observeOnChanged(this) { data ->
            appAdapter.set(data.first)
            hideSrl()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        appAdapter = object :
                BaseBindAdapter<AppItemViewModel.AppGroupItem, ItemGroupRvBinding>(R.layout.item_group_rv) {
            init {
                diffUtil = object : DiffItemCallback<AppItemViewModel.AppGroupItem>() {

                    override fun areContentsTheSame(
                            oldItem: AppItemViewModel.AppGroupItem,
                            newItem: AppItemViewModel.AppGroupItem
                    ): Boolean {
                        val old = oldItem.appItem ?: return false
                        val new = newItem.appItem ?: return false
                        return old.name == new.name && old.pkgName == new.pkgName
                    }
                }
            }

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
                        return appAdapter.get(position)!!.span2End
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
                override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                    super.onDrawOver(c, parent, state)
                    val firstPos = manager.findFirstVisibleItemPosition()
                    val endPos = manager.findLastVisibleItemPosition()
                    if (firstPos == -1 || endPos == -1) {
                        return
                    }
                    for (i in firstPos..endPos) {
                        val bean = appAdapter.get(i) ?: return
                        if (bean.indexInLetter == 0) {
                            val view = parent.getChildAt(i - firstPos) ?: return
                            c.drawTextInYCenter(bean.char.toString(), padding,
                                    view.top + padding + 50f / 2f, paint)
                        }
                    }
                }
            })
            adapter = appAdapter
        }
        bind.baseTopSrl.apply { setOnRefreshListener { postDelayed({ hideSrl() }, 600L) } }
        showSrl()
        querySize()
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
}