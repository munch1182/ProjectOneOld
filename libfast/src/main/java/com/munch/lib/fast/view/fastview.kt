@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package com.munch.lib.fast.view

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.extend.*
import com.munch.lib.android.function.ViewCreator
import com.munch.lib.android.recyclerview.BaseViewHolder
import com.munch.lib.android.recyclerview.OnItemClickListener
import com.munch.lib.android.recyclerview.SimpleViewAdapter
import com.munch.lib.fast.R
import kotlin.reflect.KClass

/**
 * 提供一个快速构成固定页面形式的方法
 */

interface FastView {

    val context: Context

    val contentView: View

    fun onCreate()

    fun init() {
        //doNothing
    }
}

fun <V : FastView> Activity.fv(creator: () -> FastView): Lazy<V> {
    return lazy {
        creator.invoke().apply {
            setContentView(contentView)
            onCreate()
        } as V
    }
}

//<editor-fold desc="view">
/**
 * 返回一个新建Textview(MW,padding(16,8,16,8))的ViewCreator
 */
private val newTextView: ViewCreator
    get() = {
        val dp16 = 16.dp2Px2Int()
        val dp10 = 10.dp2Px2Int()
        AppCompatTextView(it, null, R.attr.fastAttrTextTitle).apply {
            layoutParams = newMWLP
            setPadding(dp16, dp10, dp16, dp10)
            clickEffect()
        }
    }
//</editor-fold>

//<editor-fold desc="rv">
/**
 * 返回一个[FastRvTv]
 */
inline fun Activity.fvRvTv(str: Array<String>) = fv<FastRvTv> { FastRvTv(this, str) }

/**
 * 返回一个[FastRvTv], 其显示为[Activity]类的simpleName, 且其item点击时跳转到该[Activity]
 */
inline fun Activity.fvRvTv(clazz: Array<KClass<out Activity>>) = fv<FastRvTv> {
    FastRvTv(
        this,
        clazz.map { it.simpleName!!.replace("Activity", "") }.toTypedArray()
    ).apply {
        setOnItemClick { startActivity(clazz[it.pos]) }
    }
}

/**
 * item由[newTextView]组成的仅供显示[data]的Rv视图
 */
open class FastRvTv(override val context: Context, private val data: Array<String>) : FastView {
    protected open val rv by lazy { RecyclerView(context).apply { layoutParams = newMWLP } }
    override val contentView: View
        get() = rv
    protected open val adapter by lazy {
        SimpleViewAdapter<String>(newTextView) { h, b -> h.itemView.to<TextView>().text = b }
    }

    override fun onCreate() {
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter
        rv.addItemDecoration(LinearLineItemDecoration())
        adapter.set(data.toMutableList())
    }

    open fun setOnItemClick(l: OnItemClickListener<BaseViewHolder>?) {
        adapter.setOnItemClick(l)
    }
}
//</editor-fold>

//<editor-fold desc="tv">
/**
 * 返回一个[FastSvTv]
 */
inline fun Activity.fvSvTv() = fv<FastSvTv> { FastSvTv(this) }

/**
 * 一个ScrollView其中由text组成
 */
open class FastSvTv(override val context: Context) : FastView {
    private val text by lazy { newTextView.invoke(context).to<TextView>() }
    private val view by lazy { ScrollView(context).apply { addView(text) } }
    override val contentView: View
        get() = view

    override fun onCreate() {
    }

    fun set(str: CharSequence) {
        text.text = str
    }

    fun append(str: CharSequence) {
        text.text = "${text.text}$str"
    }
}
//</editor-fold>
