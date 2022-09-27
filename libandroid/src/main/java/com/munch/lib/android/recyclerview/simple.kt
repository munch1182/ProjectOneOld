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
 *
 * [SimpleAdapter]: 传入LayoutRes即可实现Adapter
 * [SimpleViewAdapter]: 传入View即可实现Adapter
 * [SimpleBaseBindAdapter]: 继承并实现并声明VB即可实现Adapter
 */

open class SimpleVH(view: View) : RecyclerView.ViewHolder(view)

class SimpleAdapter<D>(@LayoutRes resId: Int, private val bind: (SimpleVH, D) -> Unit) :
    BaseRecyclerViewAdapter<D, SimpleVH>({ parent, _ -> SimpleVH(parent.inflate(resId)) }) {
    override fun onBind(holder: SimpleVH, bean: D) = bind.invoke(holder, bean)
}

abstract class SimpleBaseViewAdapter<D>(
    vr: ViewCreator,
    dataHelper: AdapterFunHelper<D> = SimpleAdapterFun()
) : BaseRecyclerViewAdapter<D, SimpleVH>(
    { parent, _ -> SimpleVH(vr.invoke(parent.context)) },
    dataHelper
)

class SimpleViewAdapter<D>(vr: ViewCreator, private val bind: (BaseViewHolder, D) -> Unit) :
    SimpleBaseViewAdapter<D>(vr) {
    override fun onBind(holder: SimpleVH, bean: D) = bind.invoke(holder, bean)
}

/**
 * 使用了反射来获取ItemView
 *
 * 因为使用了泛型来进行反射, 所以必须继承着使用
 */
abstract class SimpleBaseBindAdapter<D, VB : ViewBinding, VH : BBVH<VB>>(
    dataHelper: AdapterFunHelper<D> = SimpleAdapterFun(),
    eventHelper: AdapterEventHelper<VH> = ClickHelper()
) : BaseRecyclerViewAdapter<D, VH>(null, dataHelper, eventHelper) {

    private val method by lazy {
        this.javaClass.findParameterized(ViewBinding::class.java)
            ?.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
            )
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = method?.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB
        return (BBVH(view) as VH).apply { eventHelper.onCreate(this) }
    }
}

/**
 * 必须继承着使用
 */
abstract class SimpleBindAdapter<D, VB : ViewBinding> : SimpleBaseBindAdapter<D, VB, BBVH<VB>>()
