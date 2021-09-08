package com.munch.lib.recyclerview

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil

/**
 * Create by munch1182 on 2021/8/5 17:01.
 */
interface AdapterFun<D> : IsAdapter {

    val data: MutableList<D?>

    /**
     * 如果使用了[differ]，则有些方法是否生效还受到[DiffUtil.ItemCallback]以及引用关系的影响
     */
    val differ: AsyncListDiffer<D>?

    //<editor-fold desc="set">
    /**
     * 如果[differ]不为null，则优先使用[differ]实现数据集合的更新
     */
    fun set(newData: MutableList<D?>?, runnable: Runnable? = null) {
        if (differ == null) {
            val size = data.size
            data.clear()
            noTypeAdapter.notifyItemRangeRemoved(0, size)
            if (newData != null) {
                data.addAll(newData)
                noTypeAdapter.notifyItemRangeInserted(0, newData.size)
            }
        } else {
            differ!!.submitList(newData, runnable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="add">
    fun add(element: D?) = add(data.size, element)

    fun add(index: Int, element: D?) {
        if (index in 0..data.size) {
            data.add(index, element)
            noTypeAdapter.notifyItemInserted(index)
        }
    }

    fun add(elements: Collection<D?>) = add(data.size, elements)

    /**
     * 从[index]位置起，插入数据列表[elements]
     */
    fun add(index: Int, elements: Collection<D?>) {
        if (index in 0..data.size) {
            data.addAll(index, elements)
            noTypeAdapter.notifyItemRangeInserted(index, elements.size)
        }
    }
    //</editor-fold>

    //<editor-fold desc="remove">
    fun remove(element: D) {
        val pos = data.indexOf(element)
        if (pos == -1) {
            return
        }
        data.remove(element)
        noTypeAdapter.notifyItemRemoved(pos)
    }

    fun remove(index: Int) = remove(index, 1)

    /**
     * 从[startIndex]开始删除[size]个元素
     */
    fun remove(startIndex: Int, size: Int) {
        val endIndex = startIndex + size
        if (endIndex < data.size) {
            val subList = data.subList(startIndex, endIndex)
            data.removeAll(subList)
            noTypeAdapter.notifyItemRangeRemoved(startIndex, size)
        }
    }

    /**
     * 删除与[element]相同的元素:相同取决于元素的equal实现
     * [element]不必连续，如果连续，优先使用[remove]的带索引的方法
     */
    fun remove(element: Collection<D?>) {
        data.removeAll(element)
        element.forEach {
            val index = getIndex(it ?: return@forEach) ?: return@forEach
            noTypeAdapter.notifyItemRemoved(index)
        }
    }
    //</editor-fold>

    //<editor-fold desc="update">
    /**
     * 如果[index]在数据范围内，则更新[index]位置的数据为[element]
     *
     * 如果使用了[differ]，[element]必须是一个新值，否则不会更新
     *
     * @see updateOrThrow
     */
    fun update(index: Int, element: D?) {
        val size = data.size
        if (index in 0 until size) {
            data[index] = element
            noTypeAdapter.notifyItemChanged(index)
        }
    }

    /**
     * 更新[index]位置的数据为[element]，如果[index]超出数据范围，则抛出异常
     *
     * @see update
     */
    fun updateOrThrow(index: Int, element: D?) {
        if (data.size <= index) {
            throw IndexOutOfBoundsException()
        }
        update(index, element)
    }

    /**
     * 如果在原有数据长度内，则从[startIndex]开始，更改[elements]长度的元素为[elements]
     *
     * @see updateOrThrow
     */
    fun update(startIndex: Int, elements: Collection<D?>) {
        val size = data.size
        val updateCount = elements.size
        //如果更改的数据在原有数据范围内
        if ((startIndex + updateCount) in 0 until size) {
            elements.forEachIndexed { index, d -> data[startIndex + index] = d }
            noTypeAdapter.notifyItemRangeChanged(startIndex, updateCount)
        }
    }

    /**
     * 从[startIndex]开始，更改[elements]长度的元素，如果[elements]元素超出原有数据长度，则会抛出异常
     *
     * @see update
     */
    fun updateOrThrow(startIndex: Int, elements: Collection<D?>) {
        val size = data.size
        val updateCount = elements.size
        //如果更改的数据在原有数据范围内
        if ((startIndex + updateCount) >= size) {
            throw IndexOutOfBoundsException()
        }
        update(startIndex, elements)
    }
    //</editor-fold>

    //<editor-fold desc="get">
    fun get(index: Int) = if (data.size <= index) null else data[index]
    fun getIndex(element: D): Int? = data.indexOf(element).takeIf { it != -1 }
    fun contains(element: D): Boolean = data.contains(element)
    //</editor-fold>
}