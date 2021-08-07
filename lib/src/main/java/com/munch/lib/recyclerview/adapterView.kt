package com.munch.lib.recyclerview

import android.util.SparseArray
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.util.contains
import com.munch.lib.base.ViewCreator

/**
 * Create by munch1182 on 2021/8/5 17:58.
 */

interface AdapterViewImp {

    fun createVH(parent: ViewGroup, viewType: Int): BaseViewHolder

    fun getItemViewType(position: Int): Int = 0

    fun createViewByView(parent: ViewGroup, itemView: View): View {
        return itemView
    }

    fun createViewByRes(parent: ViewGroup, @LayoutRes res: Int): View {
        return LayoutInflater.from(parent.context).inflate(res, parent, false)
    }
}

/**
 * 指定单视图类型
 *
 * @see SingleViewHelper
 * @see BaseRecyclerViewAdapter.checkAdapterView
 */
interface SingleViewModule : IsAdapter {

    val singleViewHelper: SingleViewHelper
        get() {
            val viewHelper = this.noTypeAdapter.adapterViewHelper
            check(viewHelper is SingleViewHelper) { "在SingleTypeViewModule下的实现必须是SingleTypeViewHelper" }
            return viewHelper
        }
}

class SingleViewHelper(
    private var viewCreator: ViewCreator? = null,
    private var layoutResId: Int = 0
) : AdapterViewImp {

    fun setContentView(itemView: ViewCreator) {
        this.viewCreator = itemView
    }

    fun setContentView(@LayoutRes resId: Int) {
        this.layoutResId = resId
    }

    override fun createVH(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = when {
            //先检查是否设置了布局文件
            layoutResId != 0 -> createViewByRes(parent, layoutResId)
            //如果没有布局，则再检查是否设置了view
            viewCreator != null -> createViewByView(parent, viewCreator!!.create(parent.context))
            //如果都没有，则抛出异常
            else -> throw IllegalStateException()
        }
        return BaseViewHolder(itemView)
    }
}

interface ItemViewTypeGetter {

    fun getItemViewType(pos: Int): Int
}

/**
 * 指定多视图类型
 *
 * @see MultiViewHelper
 * @see BaseRecyclerViewAdapter.checkAdapterView
 */
interface MultiViewModule : IsAdapter {

    val multiViewHelper: MultiViewHelper
        get() {
            val viewHelper = this.noTypeAdapter.adapterViewHelper
            check(viewHelper is MultiViewHelper) { "在MultiTypeViewModule下的实现必须是MultiTypeViewHelper" }
            return viewHelper
        }

}

/**
 * 多布局的实现，先调用[setType]设置多布局的类型，再调用[setTypeView]来设置多布局的视图即可
 */
class MultiViewHelper : AdapterViewImp {
    private var viewLayoutResMap: SparseIntArray? = null
    private var viewCreatorMap: SparseArray<ViewCreator>? = null
    private var typeGetter: ItemViewTypeGetter? = null

    companion object {

        private const val DEF_INITIAL = 2
    }

    /**
     * 为数据分类
     *
     * @see getItemViewType
     */
    fun setType(pos: ItemViewTypeGetter): MultiViewHelper {
        typeGetter = pos
        return this
    }

    /**
     * 为[type]类型设置一个[layoutRes]布局
     *
     * @see createVH
     */
    fun setTypeView(type: Int, @LayoutRes layoutRes: Int): MultiViewHelper {
        if (viewLayoutResMap == null) {
            viewLayoutResMap = SparseIntArray(DEF_INITIAL)
        }
        viewLayoutResMap?.put(type, layoutRes)
        return this
    }

    /**
     * 为[type]类型设置一个[creator]来生成视图
     *
     * @see createVH
     */
    fun setTypeView(type: Int, creator: ViewCreator): MultiViewHelper {
        if (viewCreatorMap == null) {
            viewCreatorMap = SparseArray(DEF_INITIAL)
        }
        viewCreatorMap?.put(type, creator)
        return this
    }

    override fun getItemViewType(position: Int): Int {
        return typeGetter?.getItemViewType(position) ?: super.getItemViewType(position)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): BaseViewHolder {
        check(viewLayoutResMap != null || viewCreatorMap != null) { "视图不能为空" }

        val itemView = if (viewLayoutResMap != null && viewLayoutResMap!!.contains(viewType)) {
            createViewByRes(parent, viewLayoutResMap!!.get(viewType))
        } else if (viewCreatorMap != null && viewCreatorMap!!.indexOfKey(viewType) >= 0) {
            createViewByView(parent, viewCreatorMap!!.get(viewType).create(parent.context))
        } else {
            throw IllegalStateException("未设置该类型的视图")
        }
        return BaseViewHolder(itemView)
    }
}