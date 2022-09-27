package com.munch.lib.android.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.munch.lib.android.define.ViewCreator
import com.munch.lib.android.extend.findParameterized
import com.munch.lib.android.extend.inflate
import com.munch.lib.android.extend.lazy
import androidx.recyclerview.widget.RecyclerView.ViewHolder as BaseViewHolder
import com.munch.lib.android.recyclerview.BaseBindViewHolder as BBVH

/**
 * 将需要实现的参数作为参数传入,其余的使用默认参数, 方便最快的实现
 */

/**
 * [RecyclerView.ViewHolder]的一个默认实现, 无其它添加实现
 */
class SimpleVH(view: View) : RecyclerView.ViewHolder(view)

/**
 * 默认实现为[SimpleVH]
 */
abstract class BaseSingleViewAdapter<D>(
    vhProvider: VHProvider<SimpleVH>?,
    dataHelper: AdapterFunHelper<D> = SimpleAdapterFun(),
    eventHelper: AdapterEventHelper<SimpleVH> = ClickHelper()
) : BaseRecyclerViewAdapter<D, SimpleVH>(vhProvider, dataHelper, eventHelper)

/**
 * 简单Adapter, 可直接调用而不需要继承
 *
 * @param resId 布局作为item的view
 * @param bind 将需要继承的实现作为参数传入
 */
class SimpleAdapter<D>(@LayoutRes resId: Int, private val bind: (SimpleVH, D) -> Unit) :
    BaseSingleViewAdapter<D>({ SimpleVH(it.inflate(resId)) }) {
    override fun onBind(holder: SimpleVH, bean: D) = bind.invoke(holder, bean)
}

/**
 * 简单Adapter, 可直接调用而不需要继承
 *
 * @param vr 创建的view作为item的view
 * @param bind 将需要继承的实现作为参数传入
 */
class SimpleViewAdapter<D>(vr: ViewCreator, private val bind: (BaseViewHolder, D) -> Unit) :
    BaseSingleViewAdapter<D>({ SimpleVH(vr.invoke(it.context)) }) {
    override fun onBind(holder: SimpleVH, bean: D) = bind.invoke(holder, bean)
}

/**
 * 使用泛型[VB]来获取itemView, 其ViewHolder为[BBVH]
 *
 * 使用了反射来获取ItemView
 * 因为使用了泛型来进行反射, 所以必须继承着使用
 */
abstract class SimpleBaseBindAdapter<D, VB : ViewBinding>(
    dataHelper: AdapterFunHelper<D> = SimpleAdapterFun(),
    eventHelper: AdapterEventHelper<BBVH<VB>> = ClickHelper()
) : BaseRecyclerViewAdapter<D, BBVH<VB>>(null, dataHelper, eventHelper) {

    private val method by lazy {
        this.javaClass.findParameterized(ViewBinding::class.java)
            ?.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
            )
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BBVH<VB> {
        val view = method?.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB
        return BBVH(view).apply { eventHelper.onCreate(this) }
    }
}

abstract class BaseMultiViewAdapter<D : Any>(
    dataHelper: AdapterFunHelper<D> = SimpleAdapterFun(),
    eventHelper: AdapterEventHelper<SimpleVH> = ClickHelper()
) : BaseMultiRecyclerViewAdapter<D, SimpleVH>(
    dataHelper = dataHelper, eventHelper = eventHelper
)