@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.fast.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.button.MaterialButton
import com.munch.lib.InitFunInterface
import com.munch.lib.OnViewTagClickListener
import com.munch.lib.extend.LinearLineItemDecoration
import com.munch.lib.extend.clickItem
import com.munch.lib.extend.newMWLp
import com.munch.lib.fast.R
import com.munch.lib.recyclerview.*
import com.munch.lib.weight.FlowLayout
import com.munch.lib.weight.Space
import kotlin.reflect.KClass

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

    fun onViewAdd()

    override fun init() {
        //nothing
    }
}

//<editor-fold desc="FastView,主题需要App.Fast">
/**
 * 传入adapter实现RecyclerView布局
 */
inline fun <D> Activity.fvRv(
    adapter: BaseRecyclerViewAdapter<D, BaseViewHolder>,
    lm: RecyclerView.LayoutManager = LinearLayoutManager(this)
) = fv<FVRecyclerView<D, BaseViewHolder>> { FVRecyclerView(this, adapter, lm) }

inline fun <D, B : ViewBinding> Activity.fvBindRv(
    adapter: BaseRecyclerViewAdapter<D, BaseBindViewHolder<B>>,
    lm: RecyclerView.LayoutManager = LinearLayoutManager(this)
) = fv<FVRecyclerView<D, BaseViewHolder>> { FVRecyclerView(this, adapter, lm) }

inline fun <D> Activity.fvHelperRv(
    adapter: BaseRecyclerViewAdapter<D, BaseViewHolder>,
    lm: RecyclerView.LayoutManager = LinearLayoutManager(this)
) = fv<FVRecyclerHelperView<D, BaseViewHolder>> {
    FVRecyclerHelperView(this, AdapterHelper(adapter), lm)
}

inline fun <D, B : ViewBinding> Activity.fvHelperBindRv(
    adapter: BaseRecyclerViewAdapter<D, BaseBindViewHolder<B>>,
    lm: RecyclerView.LayoutManager = LinearLayoutManager(this)
) = fv<FVRecyclerHelperView<D, BaseBindViewHolder<B>>> {
    FVRecyclerHelperView(this, AdapterHelper(adapter), lm)
}

/**
 * 两行TextView的RecyclerView
 */
inline fun Activity.fvLinesRv(
    str: List<Pair<String, String>>,
    funImp: AdapterFunImp<Pair<String, String>> = AdapterFunImp.Default()
) = fv<FVLinesRvView> { FVLinesRvView(this, str, funImp) }

/**
 * 单行TextView的RecyclerView
 */
inline fun Activity.fvLineRv(str: List<String>) = fv<FVLineRvView> { FVLineRvView(this, str) }

/**
 * 显示由Class名组成的Rv，点击即可跳转该Class(如果该Class可跳转)
 */
inline fun Activity.fvClassRv(target: List<KClass<*>>) =
    fv<FVLineRvView> {
        object : FVLineRvView(this,
            target.map { it.simpleName?.replace("Activity", "") ?: "" }) {

            override fun onViewAdd() {
                super.onViewAdd()
                adapter.setOnItemClickListener { _, pos, _ ->
                    target.getOrNull(pos)?.let {
                        try {
                            startActivity(Intent(this@fvClassRv, it.java))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

/**
 * 包裹Btn的FlowView
 */
inline fun Activity.fvFv(names: Array<String>) = fv<FVFlowView> { FVFlowView(this, names) }
//</editor-fold>

//<editor-fold desc="FastViewImp">
@Suppress("UNCHECKED_CAST")
inline fun <D : IFastView> Activity.fv(
    crossinline creator: () -> IFastView
): Lazy<D> {
    return lazy {
        creator.invoke().apply {
            setContentView(onAddView(), lp)
            onViewAdd()
        } as D
    }
}

open class FVRecyclerView<D, B : BaseViewHolder>(
    override val context: Context,
    val adapter: BaseRecyclerViewAdapter<D, B>,
    private val lm: RecyclerView.LayoutManager = LinearLayoutManager(context)
) : IFastView {

    protected val view by lazy {
        RecyclerView(context).apply { layoutParams = newMWLp() }
    }

    override fun onAddView() = view

    override fun onViewAdd() {
        view.layoutManager = lm
        view.adapter = adapter
    }
}

open class FVRecyclerHelperView<D, B : BaseViewHolder>(
    override val context: Context,
    val adapter: AdapterHelper<D, B>,
    private val lm: RecyclerView.LayoutManager = LinearLayoutManager(context)
) : IFastView {

    protected val view by lazy { RecyclerView(context) }

    override fun onAddView() = view

    val rv: RecyclerView = view

    override fun onViewAdd() {
        adapter.bind(view)
        view.layoutManager = lm
        if (lm is LinearLayoutManager) {
            view.addItemDecoration(LinearLineItemDecoration(lm))
        }
    }
}

open class FVLineRvView(context: Context, str: List<String>) :
    FVRecyclerView<String, BaseViewHolder>(context,
        object : BaseRecyclerViewAdapter<String, BaseViewHolder>({ ctx ->
            // 此方法添加的view宽度无法铺满
            TextView(ctx, null, R.attr.fastAttrTvLine)
        }) {

            init {
                set(str)
            }


            override fun onBind(holder: BaseViewHolder, position: Int, bean: String) {
                (holder.itemView as? TextView)?.apply {
                    // 先这样处理铺满
                    layoutParams = newMWLp()
                }?.text = bean
            }
        }) {


    override fun onViewAdd() {
        super.onViewAdd()
        view.setBackgroundColor(Color.WHITE)
        view.addItemDecoration(LinearLineItemDecoration(view.layoutManager as LinearLayoutManager))
    }
}

class FVLinesRvView(
    context: Context,
    str: List<Pair<String, String>>,
    funImp: AdapterFunImp<Pair<String, String>> = AdapterFunImp.Default()
) : FVRecyclerHelperView<Pair<String, String>, BaseViewHolder>(
    context,
    AdapterHelper(object :
        BaseRecyclerViewAdapter<Pair<String, String>, BaseViewHolder>({ ctx ->
            LinearLayout(ctx, null, R.attr.fastAttrLineVertical).apply {
                addView(TextView(ctx, null, R.attr.fastAttrTvNormal))
                addView(TextView(ctx, null, R.attr.fastAttrTvDesc))
            }
        }, funImp) {

        init {
            if (str.isNotEmpty()) {
                set(str)
            }
        }

        override fun onBind(holder: BaseViewHolder, position: Int, bean: Pair<String, String>) {
            val vg = holder.itemView.apply { layoutParams = newMWLp() } as? ViewGroup ?: return
            (vg.getChildAt(0) as? TextView)?.text = bean.first
            (vg.getChildAt(1) as? TextView)?.text = bean.second
        }
    })
) {
    override fun onViewAdd() {
        super.onViewAdd()
        view.setBackgroundColor(Color.WHITE)
        view.addItemDecoration(LinearLineItemDecoration(view.layoutManager as LinearLayoutManager))
    }
}

class FVFlowView(override val context: Context, names: Array<String> = arrayOf()) : IFastView {

    private val desc by lazy { TextView(context, null, R.attr.fastAttrTvDesc) }
    private val view = FlowLayout(context, null, R.attr.fastAttrContainer).apply {
        names.forEach { if (it.isEmpty()) addView(Space(context)) else addView(childBtn(it)) }
        addView(Space(context))
        addView(desc)
    }

    private fun childBtn(str: String) = MaterialButton(context).apply { text = str }

    override fun onAddView() = ScrollView(context).apply { addView(view) }

    override fun onViewAdd() {
    }

    fun desc(charSequence: CharSequence?) {
        desc.text = charSequence
    }

    fun append(charSequence: CharSequence?) {
        desc.text = buildString {
            append(desc.text)
            append("\n")
            append(charSequence)
        }
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
//</editor-fold>