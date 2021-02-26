package com.munch.project.launcher.appitem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.helper.drawTextInYCenter
import com.munch.project.launcher.R
import com.munch.project.launcher.appitem.AppShowAdapterHelper.Companion.offsetPos
import com.munch.project.launcher.base.recyclerview.BaseBindAdapter
import com.munch.project.launcher.base.recyclerview.BaseBindViewHolder
import com.munch.project.launcher.base.recyclerview.StatusBarAdapter
import com.munch.project.launcher.databinding.ItemAppBeanBinding
import java.util.*

/**
 * Create by munch1182 on 2021/2/24 11:13.
 */
class AppItemAdapter : BaseBindAdapter<AppShowBean, ItemAppBeanBinding>(R.layout.item_app_bean) {

    override fun onBind(
        holder: BaseBindViewHolder<ItemAppBeanBinding>,
        data: AppShowBean,
        position: Int
    ) {
        holder.binding.app = data
    }
}

/**
 * 统一管理adapter，处理头部尾部以及相关数据的偏移
 *
 * @see getAdapter
 *
 * @see offsetPos
 */
class AppShowAdapterHelper(context: Context) {

    private val statusBarAdapter = StatusBarAdapter(context)
    private val appItemAdapter = AppItemAdapter()
    private val concatAdapter = ConcatAdapter(statusBarAdapter, appItemAdapter)

    fun getAdapter() = concatAdapter
    fun getItemAdapter() = appItemAdapter

    companion object {

        fun offsetPos(pos: Int): Int {
            if (pos == 0) {
                return -1
            }
            return pos - 1
        }

        fun resume2Pos(pos: Int) = pos + 1

        fun isNotItemPos(pos: Int): Boolean {
            return pos == 0
        }
    }
}

class AppShowLayoutManager(
    context: Context,
    spanCount: Int,
    itemBean: MutableList<AppShowBean>
) : GridLayoutManager(context, spanCount) {

    init {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                //pos为0时是StatusBarAdapter
                if (AppShowAdapterHelper.isNotItemPos(position)) {
                    return spanCount
                }
                return itemBean[offsetPos(position)].showParameter?.space2End!!
            }
        }

    }
}

class NavItemDecoration(private val data: MutableList<AppShowBean>) :
    RecyclerView.ItemDecoration() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 45f
        color = Color.BLACK
    }

    /*此方法绘制需要rv的paddingStart配合*/
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        var child: View?
        for (i in 0 until parent.childCount) {
            child = parent.getChildAt(i)
            val pos = parent.getChildAdapterPosition(child)
            if (AppShowAdapterHelper.isNotItemPos(pos) || pos == -1) {
                continue
            }
            val item = data[offsetPos(pos)]
            item.showParameter ?: continue
            if (item.showParameter!!.indexInLetter == 0) {
                c.drawTextInYCenter(
                    item.letterChar.toString().toUpperCase(Locale.ROOT),
                    parent.paddingStart / 3f, child.height / 3f + child.y, paint
                )
            }
        }
    }

    //<editor-fold desc="Deprecated func">
    /*
    /**
     * 因为单第一列的偏移无法保证均分而不挤压，所以不采用这种方式，而采用rv的paddingStart的方式
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        *//*for (i in 0 until parent.childCount) {
                val pos = parent.getChildAdapterPosition(view)
                if (AppShowAdapterHelper.isNotItemPos(pos) || pos == -1) {
                    continue
                }
                val appShowBean = data[AppShowAdapterHelper.offsetPos(pos)]
                val indexInLetter = appShowBean.showParameter?.indexInLetter ?: continue
                val space = 5
                if (indexInLetter % spanCount == 0) {
                    outRect.set(space + NAV_WIDTH, space, space, space)
                } else {
                    outRect.set(space, space, space, space)
                }
            }*//*
        }*/
    //</editor-fold>
}
