package com.munch.lib.recyclerview

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.munch.lib.extend.isMain
import com.munch.lib.helper.ThreadHelper
import com.munch.lib.recyclerview.AdapterFunImp.Default
import com.munch.lib.recyclerview.AdapterFunImp.Differ

interface DifferProvider<D> {
    fun registerDiffer(callback: DiffUtil.ItemCallback<D>): DifferProvider<D>
}

interface AdapterProvider {
    fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>)
}

/**
 * 因为[Default]/[Differ]的实现中都有实际Adapter方法调用的线程判断
 * （[Default]直接依赖[Default.mainHandler]实现,而[Differ]是依赖[AsyncListDiffer.mUpdateCallback]来实现）
 * 因此更建议在子线程中调用这些方法
 *
 * Create by munch1182 on 2021/8/5 17:01.
 */
sealed class AdapterFunImp<D>(
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
) : IAdapterFun<D> {

    protected var adapter: BaseRecyclerViewAdapter<*, *>? = null
    internal open val queryList: MutableList<D>
        get() = mutableListOf()
    internal open val updateList: MutableList<D>
        get() = mutableListOf()

    protected fun impInMain(imp: () -> Unit) {
        if (Thread.currentThread().isMain()) {
            imp.invoke()
        } else {
            mainHandler.post(imp)
        }
    }

    override fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>) {
        this.adapter = adapter
    }

    open class Default<D>(
        mainHandler: Handler = ThreadHelper.mainHandler
    ) : AdapterFunImp<D>(mainHandler) {

        private val list = mutableListOf<D>()
        override val queryList: MutableList<D>
            get() = list
        override val updateList: MutableList<D>
            get() = list

        override val itemSize: Int
            get() = queryList.size

        @SuppressLint("NotifyDataSetChanged")
        override fun set(newData: List<D>?) {
            val size = itemSize
            updateList.clear()
            if (newData != null) {
                updateList.addAll(newData)
            }
            val adapter = adapter ?: return
            val newSize = itemSize
            impInMain {
                when {
                    size == 0 -> adapter.notifyItemRangeInserted(0, itemSize)
                    newSize == 0 -> adapter.notifyItemRangeRemoved(0, size)
                    else -> adapter.notifyDataSetChanged()
                }
            }
        }

        override fun add(index: Int, element: D) {
            if (index in 0..itemSize) {
                updateList.add(index, element)
                val adapter = adapter ?: return
                impInMain { adapter.notifyItemInserted(index) }
            }
        }

        override fun add(index: Int, elements: Collection<D>) {
            if (index in 0..itemSize) {
                val size = elements.size
                updateList.addAll(index, elements)
                val adapter = adapter ?: return
                impInMain { adapter.notifyItemRangeInserted(index, size) }
            }
        }

        override fun remove(element: D) {
            val pos = queryList.indexOf(element)
            if (pos == -1) {
                return
            }
            updateList.remove(element)
            val adapter = adapter ?: return
            impInMain { adapter.notifyItemRemoved(pos) }
        }

        override fun remove(startIndex: Int, size: Int) {
            val endIndex = startIndex + size
            if (endIndex <= itemSize) {
                updateList.removeAll(updateList.subList(startIndex, endIndex))
                val adapter = adapter ?: return
                impInMain { adapter.notifyItemRangeRemoved(startIndex, size) }
            }
        }

        override fun remove(element: Collection<D?>) {
            updateList.removeAll(element.toSet())
            element.forEach {
                val index = getIndex(it ?: return@forEach) ?: return@forEach
                val adapter = adapter ?: return@forEach
                impInMain { adapter.notifyItemRemoved(index) }
            }
        }

        override fun update(index: Int, element: D, payload: Any?) {
            val size = itemSize
            if (index in 0 until size) {
                queryList[index] = element
                val adapter = adapter ?: return
                impInMain { adapter.notifyItemChanged(index, payload) }
            }
        }

        override fun update(start: Int, end: Int, payload: Any?) {
            val adapter = adapter ?: return
            adapter.notifyItemRangeChanged(start, end - start, payload)
        }

        override fun update(startIndex: Int, elements: Collection<D>, payload: Any?) {
            val size = itemSize
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) in 0 until size) {
                elements.forEachIndexed { index, d -> queryList[startIndex + index] = d }
                val adapter = adapter ?: return
                impInMain {
                    adapter.notifyItemRangeChanged(startIndex, updateCount, payload)
                }
            }
        }

        override fun add(element: D) = add(itemSize, element)
        override fun add(elements: Collection<D>) = add(itemSize, elements)
        override fun remove(index: Int) = remove(index, 1)

        override fun updateOrThrow(index: Int, element: D, payload: Any?) {
            if (itemSize <= index) {
                throw IndexOutOfBoundsException()
            }
            update(index, element, payload)
        }

        override fun updateOrThrow(startIndex: Int, elements: Collection<D>, payload: Any?) {
            val size = itemSize
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) >= size) {
                throw IndexOutOfBoundsException()
            }
            update(startIndex, elements, payload)
        }

        override fun get(index: Int) = if (itemSize <= index) null else queryList[index]
        override fun getIndex(element: D): Int? = queryList.indexOf(element).takeIf { it != -1 }
        override fun contains(element: D): Boolean = queryList.contains(element)
    }

    /**
     * 注意[differ]的实现，特别是[androidx.recyclerview.widget.ListUpdateCallback]的实现，因为
     * [androidx.recyclerview.widget.AsyncListDiffer.submitList]的回调线程不是固定的
     */
    class Differ<D>(
        private val callback: DiffUtil.ItemCallback<D>,
        mainHandler: Handler = ThreadHelper.mainHandler,
        private val runnable: Runnable? = null
    ) : Default<D>(mainHandler) {

        private var differ: AsyncListDiffer<D>? = null

        override fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>) {
            super.bindAdapter(adapter)
            differ = AsyncListDiffer(adapter, callback)
        }

        override val queryList: MutableList<D>
            get() = differ?.currentList ?: mutableListOf()

        override val updateList: MutableList<D>
            get() = differ?.currentList?.let { ArrayList(it) } ?: mutableListOf()

        override fun set(newData: List<D>?) {
            differ ?: throw IllegalArgumentException()
            if (queryList.isEmpty() || newData == null || newData.isEmpty()) {
                impInMain { differ?.submitList(newData, runnable) }
            } else {
                //此方法在newData和data为null时会直接在当前线程回调，否则会在子线程中计算，在主线程中回调
                differ?.submitList(newData, runnable)
            }
        }


        override fun add(index: Int, element: D) {
            if (index in 0..itemSize) {
                set(updateList.apply { add(index, element) })
            }
        }

        override fun add(index: Int, elements: Collection<D>) {
            if (index in 0..itemSize) {
                set(updateList.apply { addAll(index, elements) })
            }
        }

        override fun remove(element: D) {
            val pos = queryList.indexOf(element)
            if (pos == -1) return
            set(updateList.apply { remove(element) })
        }

        override fun remove(startIndex: Int, size: Int) {
            val endIndex = startIndex + size
            if (endIndex <= itemSize) {
                val l = updateList
                set(l.apply { removeAll(l.subList(startIndex, endIndex)) })
            }
        }

        override fun remove(element: Collection<D?>) {
            set(updateList.apply { removeAll(element.toSet()) })
        }

        override fun update(index: Int, element: D, payload: Any?) {
            val size = itemSize
            if (index in 0 until size) {
                set(queryList.apply { this[index] = element })
            }
        }


        override fun update(startIndex: Int, elements: Collection<D>, payload: Any?) {
            val size = itemSize
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) in 0 until size) {
                val l = queryList
                elements.forEachIndexed { index, d -> l[startIndex + index] = d }
                set(l)
            }
        }

        override fun add(element: D) = add(itemSize, element)
        override fun add(elements: Collection<D>) = add(itemSize, elements)
        override fun remove(index: Int) = remove(index, 1)
        override fun updateOrThrow(index: Int, element: D, payload: Any?) {
            if (itemSize <= index) {
                throw IndexOutOfBoundsException()
            }
            update(index, element, payload)
        }

        override fun updateOrThrow(startIndex: Int, elements: Collection<D>, payload: Any?) {
            val size = itemSize
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) >= size) {
                throw IndexOutOfBoundsException()
            }
            update(startIndex, elements, payload)
        }

        override fun get(index: Int) = if (itemSize <= index) null else queryList[index]
        override fun getIndex(element: D): Int? = queryList.indexOf(element).takeIf { it != -1 }
        override fun contains(element: D): Boolean = queryList.contains(element)
    }
}

open class AdapterFunImp2<D>(
    protected open val mainHandler: Handler = Handler(Looper.getMainLooper())
) : IAdapterFun<D>, DifferProvider<D> {

    override val itemSize: Int
        get() = imp.itemSize

    private val defImp by lazy { Default<D>(mainHandler) }
    private var diffImp: Differ<D>? = null

    protected open val imp: AdapterFunImp<D>
        get() = diffImp ?: defImp

    override fun registerDiffer(callback: DiffUtil.ItemCallback<D>): DifferProvider<D> {
        diffImp = Differ(callback, mainHandler)
        return this
    }

    override fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>) {
        imp.bindAdapter(adapter)
    }

    override fun set(newData: List<D>?) {
        imp.set(newData)
    }

    override fun add(index: Int, element: D) {
        imp.add(index, element)
    }

    override fun add(index: Int, elements: Collection<D>) {
        imp.add(index, elements)
    }

    override fun remove(element: D) {
        imp.remove(element)
    }

    override fun remove(startIndex: Int, size: Int) {
        imp.remove(startIndex, size)
    }

    override fun remove(element: Collection<D?>) {
        imp.remove(element)
    }

    override fun update(index: Int, element: D, payload: Any?) {
        imp.update(index, element, payload)
    }

    override fun update(start: Int, end: Int, payload: Any?) {
        imp.update(start, end, payload)
    }

    override fun update(startIndex: Int, elements: Collection<D>, payload: Any?) {
        imp.update(startIndex, elements, payload)
    }

    override fun add(element: D) = add(itemSize, element)
    override fun add(elements: Collection<D>) = add(itemSize, elements)
    override fun remove(index: Int) = remove(index, 1)

    override fun updateOrThrow(index: Int, element: D, payload: Any?) {
        if (itemSize <= index) {
            throw IndexOutOfBoundsException()
        }
        update(index, element, payload)
    }

    override fun updateOrThrow(startIndex: Int, elements: Collection<D>, payload: Any?) {
        val size = itemSize
        val updateCount = elements.size
        //如果更改的数据在原有数据范围内
        if ((startIndex + updateCount) >= size) {
            throw IndexOutOfBoundsException()
        }
        update(startIndex, elements, payload)
    }

    override fun get(index: Int) = if (itemSize <= index) null else imp.queryList[index]
    override fun getIndex(element: D): Int? = imp.queryList.indexOf(element).takeIf { it != -1 }
    override fun contains(element: D): Boolean = imp.queryList.contains(element)
}