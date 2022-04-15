@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.fast.view

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.base.InitFunInterface
import com.munch.lib.base.OnViewTagClickListener
import com.munch.lib.extend.*
import com.munch.lib.fast.R
import com.munch.lib.log.log
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter
import com.munch.lib.recyclerview.BaseViewHolder
import com.munch.lib.weight.FlowLayout
import com.munch.lib.weight.Space

/**
 * Created by munch1182 on 2022/4/15 23:23.
 */

interface IFastView : InitFunInterface {

    val context: Context

    val lp: ViewGroup.LayoutParams
        get() = newMWLp()

    /**
     * 当该view用于被添加时
     */
    fun onAddView(): View

    fun onViewCreate()

    override fun init() {
        //nothing
    }
}


/**
 * 主题需要引用App.Fast
 */
inline fun <D> Activity.fvRv(
    adapter: BaseRecyclerViewAdapter<D, BaseViewHolder>,
    lm: RecyclerView.LayoutManager = LinearLayoutManager(this)
) = fv<FVRecyclerView<D>> { FVRecyclerView(this, adapter, lm) }

inline fun Activity.fvLinesRv(str: List<Pair<String, String>>) =
    fv<FVLinesRvView> { FVLinesRvView(this, str) }

inline fun Activity.fvLineRv(str: List<String>) = fv<FVLineRvView> { FVLineRvView(this, str) }
inline fun Activity.fvFv(names: Array<String>) = fv<FVFlowView> { FVFlowView(this, names) }

@Suppress("UNCHECKED_CAST")
inline fun <D : IFastView> Activity.fv(
    crossinline creator: () -> IFastView
): Lazy<D> {
    return lazy {
        creator.invoke().apply {
            setContentView(onAddView(), lp)
            onViewCreate()
        } as D
    }
}

open class FVRecyclerView<D>(
    override val context: Context,
    val adapter: BaseRecyclerViewAdapter<D, BaseViewHolder>,
    private val lm: RecyclerView.LayoutManager = LinearLayoutManager(context)
) : IFastView {

    protected val view by lazy { RecyclerView(context) }

    override fun onAddView() = view

    override fun onViewCreate() {
        view.layoutManager = lm
        view.adapter = adapter
    }
}

class FVLineRvView(context: Context, str: List<String>) : FVRecyclerView<String>(context,
    object : BaseRecyclerViewAdapter<String, BaseViewHolder>({ ctx ->
        TextView(ctx, null, R.attr.fastAttrTvLine)
    }) {

        init {
            set(str)
        }

        override fun onBind(holder: BaseViewHolder, position: Int, bean: String) {
            (holder.itemView as? TextView)?.text = bean
        }
    }) {


    override fun onViewCreate() {
        super.onViewCreate()
        view.setBackgroundColor(Color.WHITE)
        view.addItemDecoration(LinearLineItemDecoration(view.layoutManager as LinearLayoutManager))
    }
}

class FVLinesRvView(context: Context, str: List<Pair<String, String>>) :
    FVRecyclerView<Pair<String, String>>(context,
        object : BaseRecyclerViewAdapter<Pair<String, String>, BaseViewHolder>({ ctx ->
            LinearLayout(ctx, null, R.attr.fastAttrLinearLayout).apply {
                addView(TextView(ctx, null, R.attr.fastAttrTvNormal))
                addView(TextView(ctx, null, R.attr.fastAttrTvDesc))
            }
        }) {

            init {
                set(str)
            }

            override fun onBind(holder: BaseViewHolder, position: Int, bean: Pair<String, String>) {
                val vg = holder.itemView as? ViewGroup ?: return
                (vg.getChildAt(0) as? TextView)?.text = bean.first
                (vg.getChildAt(1) as? TextView)?.text = bean.second
            }
        }) {
}

class FVFlowView(override val context: Context, names: Array<String> = arrayOf()) : IFastView {

    private val view = FlowLayout(context).apply {
        setPadding(dp2Px(16f).toInt())
        names.forEach { if (it.isEmpty()) addView(Space(context)) else addView(childBtn(it)) }
    }

    private fun childBtn(str: String) = AppCompatButton(context).apply { text = str }

    override fun onAddView() = view

    override fun onViewCreate() {
    }

    fun click(listener: (view: View, index: Int) -> Unit) {
        view.clickItem(object : OnViewTagClickListener<Int> {
            override fun onClick(v: View?, tagVal: Int) {
                super.onClick(v, tagVal)
                listener.invoke(v!!, tagVal)
            }
        }, Button::class)
    }

    fun remove(name: String) {
        view.children.forEach {
            if ((it as? TextView)?.text == name) {
                view.removeView(it)
                return
            }
        }
    }

    fun add(name: String) {
        view.addView(childBtn(name))
    }

    fun split() {
        view.addView(Space(context))
    }
}