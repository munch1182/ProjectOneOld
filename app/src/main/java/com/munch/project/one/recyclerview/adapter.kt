package com.munch.project.one.recyclerview

import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearSmoothScroller
import com.munch.lib.android.extend.*
import com.munch.lib.android.recyclerview.*
import com.munch.lib.fast.R

/**
 * Create by munch1182 on 2022/9/27 15:22.
 */

interface ShowAdapter : RecyclerAdapterDataFun

//<editor-fold desc="singleView">
abstract class SingleViewAdapter(
    private val scroller: LinearSmoothScroller,
    dataHelper: AdapterFunHelper<RecyclerData>
) : BaseSingleViewAdapter<RecyclerData>({
    SimpleVH(TextView(it.context, null, R.attr.fastAttrText).apply {
        layoutParams = newMWLP
        padding(horizontal = 16.dp2Px2Int(), vertical = 8.dp2Px2Int())
        clickEffect()
    })
}, dataHelper), ShowAdapter {

    override fun onBind(holder: SimpleVH, bean: RecyclerData) {
        holder.itemView.to<TextView>().text = if (holder.pos == bean.id) {
            "${holder.pos}: ${bean.data}"
        } else {
            val it = "${holder.pos}(${bean.id}): ${bean.data}"
            val first = it.indexOfFirst { c -> c == '(' }
            val end = it.indexOfFirst { c -> c == ')' }
            it.color(Color.RED, first, end)
        }
    }

    override fun remove(index: Int) {
        super<BaseSingleViewAdapter>.remove(index)
        // 因为UI绑定了holder的pos, 所以需要强制更新, 实际使用时不需要, 未强制刷新点击时获取的pos也是正确的
        notifyItemRangeChanged(index, itemCount)
    }

    override fun moveTo(index: Int) {
        scroller.targetPosition = index
    }
}

class NormalAdapter(scroller: LinearSmoothScroller) :
    SingleViewAdapter(scroller, SimpleAdapterFun())

class DiffAdapter(scroller: LinearSmoothScroller) :
    SingleViewAdapter(scroller, DifferAdapterFun(differ({ data.hashCode() })))
//</editor-fold>

//<editor-fold desc="multiView">
class RecyclerDataTitle(id: Int, title: String) : RecyclerData(id, title), BaseExpendNode {
    override var isExpanded: Boolean = false
    private val child = mutableListOf<BaseNode>()

    val title: String
        get() = data

    override fun childNode(): MutableList<BaseNode> = child

    fun setChild(child: List<BaseNode>) {
        this.child.clear()
        this.child.addAll(child)
    }
}

class RecyclerDataBlank(id: Int) : RecyclerData(id, ""), BaseNode
class RecyclerDataContent(id: Int, content: String) : RecyclerData(id, content) {
    val content: String
        get() = data
}

private class TitleProvider : RecyclerItem<RecyclerDataTitle, SimpleVH>(NodeAdapter.TYPE_TITLE, {
    SimpleVH(TextView(it.context, null, R.attr.fastAttrTextTitle).apply {
        layoutParams = newMWLP
        padding(horizontal = 16.dp2Px2Int(), vertical = 8.dp2Px2Int())
        textSize = 22f
        typeface = Typeface.DEFAULT_BOLD
        clickEffect()
    })
}) {
    override fun onBind(holder: SimpleVH, bean: RecyclerDataTitle) {
        val view = holder.itemView.to<TextView>()
        if (bean.childNode().isEmpty()) {
            view.text = bean.title
        } else {
            val sign = if (bean.isExpanded) "\uD83D\uDC46" else "\uD83D\uDC47"
            view.text = "${bean.id}(${holder.pos}) ${bean.title} $sign"
        }
    }

}

private class BlankProvider : RecyclerItem<RecyclerDataBlank, SimpleVH>(NodeAdapter.TYPE_BLANK, {
    SimpleVH(TextView(it.context).apply {
        val dp16 = 16.dp2Px2Int()
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp16)
        padding(horizontal = 16.dp2Px2Int(), vertical = 8.dp2Px2Int())
        clickEffect()
    })
}) {
    override fun onBind(holder: SimpleVH, bean: RecyclerDataBlank) {
    }
}

private class ContentProvider :
    RecyclerItem<RecyclerDataContent, SimpleVH>(NodeAdapter.TYPE_CONTENT, {
        SimpleVH(TextView(it.context, null, R.attr.fastAttrText).apply {
            layoutParams = newMWLP
            padding(horizontal = 16.dp2Px2Int(), vertical = 8.dp2Px2Int())
            clickEffect()
        })
    }) {
    override fun onBind(holder: SimpleVH, bean: RecyclerDataContent) {
        holder.itemView.to<TextView>().text = "${bean.id}(${holder.pos}) ${bean.content}"
    }

}

class MultiAdapter(private val scroller: LinearSmoothScroller) :
    BaseMultiViewAdapter<RecyclerData>(), ShowAdapter {

    init {
        addItem(TitleProvider())
        addItem(BlankProvider())
        addItem(ContentProvider())
    }

    override fun moveTo(index: Int) {
        scroller.targetPosition = index
    }

}
//</editor-fold>

class NodeAdapter(private val scroller: LinearSmoothScroller) :
    BaseNodeViewAdapter<RecyclerData>(), ShowAdapter {

    companion object {
        const val TYPE_TITLE = 1
        const val TYPE_BLANK = 2
        const val TYPE_CONTENT = 3
    }

    init {
        addItem(TitleProvider())
        addItem(BlankProvider())
        addItem(ContentProvider())
    }

    override fun moveTo(index: Int) {
        scroller.targetPosition = index
    }
}