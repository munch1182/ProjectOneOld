package com.munch.lib.recyclerview

import android.os.Handler
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.munch.lib.task.isMain

/**
 * 本类中的方法要注意调用线程
 *
 * 如果[differ]不为null，则需要自行切换到主线程， @see [AdapterListUpdateInHandlerCallback]
 * 否则则需要实现[handler]来完成线程切换
 *
 * 因此这些方法推荐在子线程调用
 *
 * Create by munch1182 on 2021/8/5 17:01.
 */
@Deprecated("将两种实现分开", replaceWith = ReplaceWith("com.munch.lib.recyclerview.AdapterFunImp"))
interface AdapterFun<D> : IsAdapter {

    val data: MutableList<D?>

    /**
     * 如果使用了[differ]，则有些方法是否生效还受到[DiffUtil.ItemCallback]以及引用关系的影响
     *
     * 注意调用的线程， @see [AdapterListUpdateInHandlerCallback]
     */
    val differ: AsyncListDiffer<D>?

    /**
     * 差异计算完成的回调
     */
    val runnable: Runnable?
        get() = null

    /**
     * 主线程Handler，用以切换到主线程
     */
    val handler: Handler?
        get() = null

    //<editor-fold desc="set">

    private fun impInMain(imp: () -> Unit) {
        when {
            handler == null -> imp.invoke()
            Thread.currentThread().isMain() -> imp.invoke()
            else -> handler?.post(imp)
        }
    }

    /**
     * 如果[differ]不为null，则优先使用[differ]实现数据集合的更新
     */
    fun set(newData: List<D?>?) {
        if (differ == null) {
            val size = data.size
            if (size > 0) {
                data.clear()
                impInMain { noTypeAdapter.notifyItemRangeRemoved(0, size) }
            }
            if (newData != null) {
                data.addAll(newData)
                impInMain { noTypeAdapter.notifyItemRangeInserted(0, newData.size) }
            }
        } else {
            //此方法在newData和源数据为null时会直接在当前线程回调，否则会在子线程中计算，在主线程中回调
            differ!!.submitList(newData, runnable)
        }
    }
    //</editor-fold>

    //<editor-fold desc="add">
    fun add(element: D?) = add(data.size, element)
    fun add(elements: Collection<D?>) = add(data.size, elements)
    fun remove(index: Int) = remove(index, 1)


    fun add(index: Int, element: D?) {
        if (index in 0..data.size) {
            if (differ == null) {
                data.add(index, element)
                impInMain { noTypeAdapter.notifyItemInserted(index) }
            } else {
                set(ArrayList(data).apply { add(index, element) })
            }
        }
    }


    /**
     * 从[index]位置起，插入数据列表[elements]
     */
    fun add(index: Int, elements: Collection<D?>) {
        if (index in 0..data.size) {
            val size = elements.size
            if (differ == null) {
                data.addAll(index, elements)
                impInMain { noTypeAdapter.notifyItemRangeInserted(index, size) }
            } else {
                set(ArrayList(data).apply { addAll(index, elements) })
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="remove">
    fun remove(element: D) {
        val pos = data.indexOf(element)
        if (pos == -1) {
            return
        }
        if (differ == null) {
            data.remove(element)
            impInMain { noTypeAdapter.notifyItemRemoved(pos) }
        } else {
            set(ArrayList(data).apply { remove(element) })
        }
    }


    /**
     * 从[startIndex]开始删除[size]个元素
     */
    fun remove(startIndex: Int, size: Int) {
        val endIndex = startIndex + size
        if (endIndex <= data.size) {
            if (differ == null) {
                data.removeAll(data.subList(startIndex, endIndex))
                impInMain { noTypeAdapter.notifyItemRangeRemoved(startIndex, size) }
            } else {
                set(ArrayList(data).apply { data.removeAll(data.subList(startIndex, endIndex)) })
            }
        }
    }

    /**
     * 删除与[element]相同的元素:相同取决于元素的equal实现
     * [element]不必连续，如果连续，优先使用[remove]的带索引的方法
     */
    fun remove(element: Collection<D?>) {
        if (differ == null) {
            data.removeAll(element)
            element.forEach {
                val index = getIndex(it ?: return@forEach) ?: return@forEach
                impInMain { noTypeAdapter.notifyItemRemoved(index) }
            }
        } else {
            val newData = ArrayList(data)
            element.forEach {
                getIndex(it ?: return@forEach) ?: return@forEach
                newData.remove(it)
            }
            set(newData)
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
            if (differ == null) {
                data[index] = element
                impInMain { noTypeAdapter.notifyItemChanged(index) }
            } else {
                set(ArrayList(data).apply { set(index, element) })
            }
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
            if (differ == null) {
                elements.forEachIndexed { index, d -> data[startIndex + index] = d }
                impInMain { noTypeAdapter.notifyItemRangeChanged(startIndex, updateCount) }
            } else {
                val newData = ArrayList(data)
                elements.forEachIndexed { index, d -> data[startIndex + index] = d }
                set(newData)
            }
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