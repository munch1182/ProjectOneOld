package com.munch.project.launcher.item

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.pre.lib.base.Orientation
import com.munch.pre.lib.base.rv.DiffItemCallback
import com.munch.pre.lib.extend.observeOnChanged
import com.munch.pre.lib.extend.startActivity
import com.munch.pre.lib.helper.SwipeViewHelper
import com.munch.pre.lib.helper.drawTextInYCenter
import com.munch.pre.lib.log.log
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.base.BaseBindAdapter
import com.munch.project.launcher.base.BaseBindViewHolder
import com.munch.project.launcher.databinding.ActivityAppBinding
import com.munch.project.launcher.databinding.ItemAppItemBinding
import com.munch.project.launcher.extend.bind
import com.munch.project.launcher.extend.get

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
    private val swipeHelper by lazy { SwipeViewHelper(this) }
    private val model by get(AppViewModel::class.java)
    private val appAdapter =
        object : BaseBindAdapter<AppGroupItem, ItemAppItemBinding>(R.layout.item_app_item) {

            init {
                diffUtil = object : DiffItemCallback<AppGroupItem>() {
                    override fun areItemsTheSame(
                        oldItem: AppGroupItem,
                        newItem: AppGroupItem
                    ): Boolean {
                        return areContentsTheSame(
                            oldItem,
                            newItem
                        ) && oldItem.indexInLetter == newItem.indexInLetter
                    }

                    override fun areContentsTheSame(
                        oldItem: AppGroupItem,
                        newItem: AppGroupItem
                    ): Boolean {
                        return oldItem.name == newItem.name && oldItem.pkg == newItem.pkg
                    }
                }
            }

            override fun onBindViewHolder(
                holder: BaseBindViewHolder<ItemAppItemBinding>,
                bean: AppGroupItem,
                pos: Int
            ) {
                holder.bind.app = bean
            }
        }
    private val itemDecoration by lazy {
        object : RecyclerView.ItemDecoration() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 50f
                color = Color.BLACK
            }
            private val padding = resources.getDimension(R.dimen.padding_def)
            private val manager = bind.itemRv.layoutManager as LinearLayoutManager
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
                    val bean = appAdapter.get(i) ?: return
                    if (bean.indexInLetter == 0) {
                        val view = parent.getChildAt(i - firstPos) ?: return
                        c.drawTextInYCenter(
                            bean.letter.toString(), padding,
                            view.top + padding + 50f / 2f, paint
                        )
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this

        val gridLayoutManager =
            GridLayoutManager(this@AppActivity, model.getSpanCount().value!!).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return appAdapter.get(position)!!.span2End
                    }
                }
            }
        bind.itemRv.apply {
            layoutManager = gridLayoutManager
            setBackgroundColor(Color.WHITE)
            setPadding(100, 0, 0, 0)
            addItemDecoration(itemDecoration)
            adapter = appAdapter
        }
        setSwipe()
        model.getItems().observeOnChanged(this) {
            log(it.first)
            appAdapter.set(it.first)
        }
        model.getSpanCount().observeOnChanged(this) {
            gridLayoutManager.spanCount = it
        }
    }

    private fun setSwipe() {
        val firstAlpha = bind.appContainer.alpha
        swipeHelper.getSwipeView().orientation(Orientation.TB or Orientation.LR).apply {
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
        swipeHelper.setActivity()
    }

    override fun toggleTheme() {
        //不实现
        /*super.toggleTheme()*/
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.anim_exit_down)
    }
}