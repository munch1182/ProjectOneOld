package com.munch.pre.lib.base.rv

/**
 * Create by munch1182 on 2021/5/14 10:13.
 */
interface FastFun<D, V : BaseViewHolder> {

    fun getData(): MutableList<D>

    fun getAdapter(): BaseAdapter<D, V>

    /**
     * 避免麻烦直接返回不为空
     * 如果有边界情况，先调用[hasIndex]进行判断
     *
     * @see hasIndex
     */
    fun get(index: Int): D = getData()[index]

    fun hasIndex(index: Int) = index >= 0 && getData().size < index

    fun add(bean: D, index: Int = -1) {
        val pos: Int
        if (index == -1) {
            pos = getData().size
            getData().add(bean)
        } else {
            pos = index
            getData().add(index, bean)
        }
        getAdapter().notifyItemInserted(pos)
    }

    fun add(beanList: MutableList<D>, index: Int = -1) {
        if (!beanList.isNullOrEmpty()) {
            val start: Int
            if (index == -1) {
                start = getData().size
                getData().addAll(beanList)
            } else {
                start = index
                getData().addAll(index, beanList)
            }
            getAdapter().notifyItemRangeInserted(start, beanList.size)
        }
    }

    fun set(beanList: MutableList<D>?) {
        if (!beanList.isNullOrEmpty()) {
            getData().clear()
            getData().addAll(beanList)
        } else {
            getData().clear()
        }
        getAdapter().notifyDataSetChanged()
    }

    fun set(index: Int, bean: D) {
        getData()[index] = bean
        getAdapter().notifyItemChanged(index)
    }

    fun remove(index: Int): D {
        val element = getData().removeAt(index)
        getAdapter().notifyItemRemoved(index)
        return element
    }
}